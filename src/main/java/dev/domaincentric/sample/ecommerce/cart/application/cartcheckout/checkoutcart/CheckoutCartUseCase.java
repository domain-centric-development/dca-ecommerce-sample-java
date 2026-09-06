package dev.domaincentric.sample.ecommerce.cart.application.checkoutcart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ArticlePrice;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ArticlePriceResolver;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartValidationResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Use case for checking out a shopping cart.
 *
 * <p>This use case orchestrates the checkout process by:
 *
 * <ol>
 *   <li>Retrieving the cart
 *   <li>Fetching fresh article data for all cart items
 *   <li>Validating the cart with current availability and stock
 *   <li>Checking out (business logic in aggregate validates rules — an empty or inactive cart is
 *       refused by the aggregate, not reported as a validation error without errors)
 *   <li>Persisting the updated cart
 *   <li>Publishing domain events
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CheckoutCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class CheckoutCartUseCase implements CheckoutCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;
  private final EnrichedCartFactory enrichedCartFactory;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public CheckoutCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public CheckoutCartResult execute(final CheckoutCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());

    // Which articles do we need? Read the cart once, then fetch article data remotely - both
    // outside the transaction
    final ShoppingCart current =
        shoppingCartRepository
            .findByIdForCustomer(cartId, CustomerId.of(input.customerId()))
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));
    final Set<ProductId> productIds =
        current.items().stream().map(item -> item.productId()).collect(Collectors.toSet());
    final Map<ProductId, CartArticle> articleData = articleDataPort.getArticleData(productIds);

    // Short transaction: reload, validate, checkout, save, publish
    return transactionBoundary.inTransaction(
        () -> {
          final ShoppingCart cart =
              shoppingCartRepository
                  .findByIdForCustomer(cartId, CustomerId.of(input.customerId()))
                  .orElseThrow(
                      () -> new IllegalArgumentException("Cart not found: " + input.cartId()));
          final EnrichedCart enrichedCart = enrichedCartFactory.create(cart, articleData);
          if (!enrichedCart.isValidForCheckout()) {
            final ArticlePriceResolver priceResolver = buildResolver(articleData);
            final CartValidationResult validationResult = cart.validateForCheckout(priceResolver);
            if (!validationResult.isValid()) {
              throw new CartValidationException(validationResult);
            }
            // Nothing is wrong with the articles: the cart itself is in no state to be checked out
            // (empty, or no longer active). Let the aggregate say so in its own words.
          }
          cart.checkout();
          shoppingCartRepository.save(cart);
          eventPublisher.publishAndClearEvents(cart);
          return toResult(cart, enrichedCart);
        });
  }

  private static CheckoutCartResult toResult(
      final ShoppingCart cart, final EnrichedCart enrichedCart) {
    final List<CheckoutCartResult.CartItemSummary> items =
        enrichedCart.items().stream()
            .map(
                item ->
                    new CheckoutCartResult.CartItemSummary(
                        item.cartItemId().value().toString(),
                        item.productId().value().toString(),
                        item.quantity().value(),
                        item.currentArticle().currentPrice().amount(),
                        item.currentArticle().currentPrice().currency().getCurrencyCode()))
            .toList();
    final Money total = enrichedCart.calculateCurrentSubtotal();
    return new CheckoutCartResult(
        cart.id().value(),
        enrichedCart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode(),
        Instant.now() // Note: In production, this should come from the aggregate or an event
        );
  }

  /**
   * Builds an ArticlePriceResolver from the fetched article data. Kept for backward compatibility
   * with CartValidationResult.
   *
   * @param articleDataMap the map of product IDs to CartArticle
   * @return a resolver that provides pricing information
   */
  private ArticlePriceResolver buildResolver(final Map<ProductId, CartArticle> articleDataMap) {
    return productId -> {
      final CartArticle article = articleDataMap.get(productId);
      if (article == null) {
        // Product not found - treat as unavailable
        return new ArticlePrice(Money.euro(0.0), false, 0);
      }
      return new ArticlePrice(
          article.currentPrice(), article.isAvailable(), article.availableStock());
    };
  }

  /** Exception thrown when cart validation fails during checkout. */
  public static class CartValidationException extends RuntimeException {
    private final CartValidationResult validationResult;

    public CartValidationException(final CartValidationResult validationResult) {
      super("Cart validation failed: " + validationResult.errors().size() + " error(s)");
      this.validationResult = validationResult;
    }

    public CartValidationResult getValidationResult() {
      return validationResult;
    }
  }
}
