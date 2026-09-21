package dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ArticleNotAvailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartData;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartNotActiveException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartNotAvailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutItemsUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.EmptyCartException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticle;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutCart;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutCartFactory;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Use case for starting a checkout session from a shopping cart.
 *
 * <p>This use case creates a new checkout session by:
 *
 * <ul>
 *   <li>Loading the cart data through the Anti-Corruption Layer
 *   <li>Fetching article data (name, price, availability) via CheckoutArticleDataPort
 *   <li>Creating checkout line items with current product details
 *   <li>Assembling a {@link CheckoutCart} through the {@link CheckoutCartFactory} and refusing to
 *       start when an article is unavailable or out of stock
 *   <li>Creating and persisting the checkout session
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link StartCheckoutInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses:
 *
 * <ul>
 *   <li>Cart data through {@link CartDataPort} output port
 *   <li>Article data (name, price, availability) through {@link CheckoutArticleDataPort} output
 *       port
 * </ul>
 *
 * This isolates the Checkout context from direct coupling to other contexts' domain models.
 */
@Service
public class StartCheckoutUseCase implements StartCheckoutInputPort {

  private final CartDataPort cartDataPort;
  private final TaxCalculator taxCalculator;
  private final CheckoutCartFactory checkoutCartFactory;
  private final CheckoutArticleDataPort checkoutArticleDataPort;
  private final CheckoutSessionRepository checkoutSessionRepository;
  private final DomainEventPublisher domainEventPublisher;
  private final TransactionBoundary transactionBoundary;

  public StartCheckoutUseCase(
      final CartDataPort cartDataPort,
      final TaxCalculator taxCalculator,
      final CheckoutCartFactory checkoutCartFactory,
      final CheckoutArticleDataPort checkoutArticleDataPort,
      final CheckoutSessionRepository checkoutSessionRepository,
      final DomainEventPublisher domainEventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.cartDataPort = cartDataPort;
    this.taxCalculator = taxCalculator;
    this.checkoutCartFactory = checkoutCartFactory;
    this.checkoutArticleDataPort = checkoutArticleDataPort;
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.domainEventPublisher = domainEventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public StartCheckoutResult execute(final StartCheckoutCommand command) {
    final CartId cartId = CartId.of(command.cartId());

    // Cart and article data come from other contexts (remote-capable) - outside the transaction.
    // Scoped to the caller: a cart that is not theirs is indistinguishable from one that does not
    // exist.
    final CartData cart =
        cartDataPort
            .findById(cartId, CustomerId.of(command.customerId()))
            .orElseThrow(() -> new CartNotAvailableException(cartId));
    if (!cart.active()) {
      throw new CartNotActiveException(cartId);
    }
    if (cart.items().isEmpty()) {
      throw new EmptyCartException(cartId);
    }
    final List<ProductId> productIds =
        cart.items().stream().map(CartData.CartItemData::productId).toList();
    final Map<ProductId, CheckoutArticle> articleDataMap =
        checkoutArticleDataPort.getArticleData(productIds);
    final List<CheckoutLineItem> lineItems = new ArrayList<>();
    for (final CartData.CartItemData cartItem : cart.items()) {
      final CheckoutArticle article = articleDataMap.get(cartItem.productId());
      if (article == null) {
        throw new ArticleNotAvailableException(cartItem.productId());
      }
      lineItems.add(
          new CheckoutLineItem(
              CheckoutLineItemId.generate(),
              cartItem.productId(),
              article.name(),
              article.currentPrice(),
              cartItem.quantity(),
              article.imageUrl(),
              cartItem.positionSnapshot()));
    }

    // The enriched read model pairs each line item with its current article data, so the domain can
    // answer availability, stock and pricing questions before a session exists
    final CheckoutCart checkoutCart =
        checkoutCartFactory.create(cart.cartId(), cart.customerId(), lineItems, articleDataMap);
    if (!checkoutCart.isValidForCheckout()) {
      throw new CheckoutItemsUnavailableException(
          checkoutCart.invalidItems().stream().map(item -> item.lineItem().productId()).toList());
    }
    final Money total = checkoutCart.calculateCurrentSubtotal();

    // Short transaction: create, save, publish
    return checkoutSessionRepository.inCartSession(
        cartId,
        () ->
            transactionBoundary.inTransaction(
                () -> {
                  checkoutSessionRepository
                      .findActiveByCartId(cartId)
                      .ifPresent(
                          old -> {
                            old.supersede();
                            checkoutSessionRepository.save(old);
                          });
                  final CheckoutSession session =
                      CheckoutSession.start(
                          cart.cartId(), cart.customerId(), lineItems, total, taxCalculator);
                  checkoutSessionRepository.save(session);
                  domainEventPublisher.publishAndClearEvents(session);
                  return StartCheckoutResult.from(session);
                }));
  }
}
