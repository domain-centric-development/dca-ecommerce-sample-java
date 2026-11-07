package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult.Added;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult.ArticleNotAvailable;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult.CartNotFound;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult.CartNotModifiable;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult.InsufficientArticleStock;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartArticle;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartNotModifiableException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.Quantity;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Use case for adding an item to a shopping cart.
 *
 * <p>This use case orchestrates adding an item to the cart by:
 *
 * <ol>
 *   <li>Retrieving the article data and checking that the product exists - outside the transaction,
 *       because the port may reach another context
 *   <li>Inside one short {@link TransactionBoundary}: loading the cart, checking that the stock
 *       covers what the line would hold — its quantity plus the one added — adding the item
 *       (business logic in aggregate), persisting the cart and publishing its domain events
 *   <li>After the commit: enriching the cart with today's article data of every product it holds,
 *       so the answer prices each item at today's price, as reading the cart does
 * </ol>
 *
 * <p>Every refusal is returned as a case of {@link AddItemToCartResult}. Each one is decided before
 * the transaction writes anything, so the transaction that returns it commits nothing.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link AddItemToCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 *
 * <p><b>Bounded Context Isolation:</b> This use case accesses article data through a single output
 * port that aggregates data from multiple contexts:
 *
 * <ul>
 *   <li>{@link ArticleDataPort} - provides name (from Product), price (from Pricing), and stock
 *       (from Inventory) information
 * </ul>
 */
@Service
public class AddItemToCartUseCase implements AddItemToCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final ArticleDataPort articleDataPort;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;
  private final EnrichedCartFactory enrichedCartFactory;

  public AddItemToCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final ArticleDataPort articleDataPort,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary,
      final EnrichedCartFactory enrichedCartFactory) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.articleDataPort = articleDataPort;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
    this.enrichedCartFactory = enrichedCartFactory;
  }

  @Override
  public AddItemToCartResult execute(final AddItemToCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());
    final ProductId productId = ProductId.of(input.productId());
    final Quantity quantity = new Quantity(input.quantity());

    // Remote-capable read (Product Catalog via ACL) - outside the transaction
    final Optional<CartArticle> article = articleDataPort.getArticleData(productId);
    if (article.isEmpty()) {
      return new ArticleNotAvailable(productId);
    }
    final CartArticle cartArticle = article.get();
    final Price priceAtAddition = Price.of(cartArticle.currentPrice());

    // Short transaction: load, check the line against the stock, mutate, save, publish
    final Change change =
        transactionBoundary.inTransaction(
            () -> {
              final Optional<ShoppingCart> found =
                  shoppingCartRepository.findByIdForCustomer(
                      cartId, CustomerId.of(input.customerId()));
              if (found.isEmpty()) {
                return new Refused(new CartNotFound(cartId));
              }
              final ShoppingCart loaded = found.get();
              final int held = loaded.quantityOf(productId);
              if (!cartArticle.hasStockFor(held + quantity.value())) {
                return new Refused(
                    new InsufficientArticleStock(
                        productId, quantity.value(), held, cartArticle.availableStock()));
              }
              try {
                loaded.addItem(productId, quantity, priceAtAddition);
              } catch (final CartNotModifiableException notModifiable) {
                return new Refused(
                    new CartNotModifiable(notModifiable.cartId(), notModifiable.status()));
              }
              shoppingCartRepository.save(loaded);
              eventPublisher.publishAndClearEvents(loaded);
              return new Saved(loaded);
            });

    return switch (change) {
      case Refused refused -> refused.refusal();
      case Saved saved -> Added.from(atTodaysPrices(saved.cart()));
    };
  }

  /**
   * The cart enriched with today's article data of every product it holds - a remote-capable read,
   * so only after the commit.
   */
  private EnrichedCart atTodaysPrices(final ShoppingCart cart) {
    final Set<ProductId> productIds =
        cart.items().stream().map(CartItem::productId).collect(Collectors.toSet());
    return enrichedCartFactory.create(cart, articleDataPort.getArticleData(productIds));
  }

  /** What the transaction came to: the cart saved, or a refusal before anything was written. */
  private sealed interface Change {}

  private record Saved(ShoppingCart cart) implements Change {}

  /**
   * A refusal decided inside the transaction. It is a {@link Refusal}, so the boundary rolls the
   * transaction back - nothing was written before it, the roll-back is the net.
   */
  private record Refused(AddItemToCartResult refusal) implements Change, Refusal {}
}
