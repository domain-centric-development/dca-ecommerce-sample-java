package dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult.CartItemNotFound;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult.CartNotFound;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult.CartNotModifiable;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult.Removed;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItemNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartNotModifiableException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Use case for removing an item from a shopping cart.
 *
 * <p>This use case orchestrates removing an item from the cart by:
 *
 * <ol>
 *   <li>Inside one short {@link TransactionBoundary}: retrieving the cart, removing the item
 *       (business logic in aggregate), persisting the updated cart and publishing its domain events
 *   <li>After the commit: enriching the cart with today's article data of every product it still
 *       holds - outside the transaction, because the port may reach another context - so the answer
 *       prices each item at today's price, as reading the cart does
 * </ol>
 *
 * <p>Every refusal is returned as a case of {@link RemoveItemFromCartResult}: a cart the caller
 * does not hold, and the aggregate's refusals, caught once here. Each one is decided before the
 * transaction writes anything, so the transaction that returns it commits nothing.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RemoveItemFromCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class RemoveItemFromCartUseCase implements RemoveItemFromCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;
  private final ArticleDataPort articleDataPort;
  private final EnrichedCartFactory enrichedCartFactory;

  public RemoveItemFromCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary,
      final ArticleDataPort articleDataPort,
      final EnrichedCartFactory enrichedCartFactory) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
    this.articleDataPort = articleDataPort;
    this.enrichedCartFactory = enrichedCartFactory;
  }

  @Override
  public RemoveItemFromCartResult execute(final RemoveItemFromCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());
    final ProductId productId = ProductId.of(input.productId());

    // Short transaction: load, remove (business logic in aggregate), save, publish
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
              try {
                loaded.removeItemByProductId(productId);
              } catch (final CartNotModifiableException notModifiable) {
                return new Refused(
                    new CartNotModifiable(notModifiable.cartId(), notModifiable.status()));
              } catch (final CartItemNotFoundException notHeld) {
                return new Refused(new CartItemNotFound(productId));
              }
              shoppingCartRepository.save(loaded);
              eventPublisher.publishAndClearEvents(loaded);
              return new Saved(loaded);
            });

    return switch (change) {
      case Refused refused -> refused.refusal();
      case Saved saved -> Removed.from(atTodaysPrices(saved.cart()));
    };
  }

  /**
   * The cart enriched with today's article data of every product it still holds - a remote-capable
   * read, so only after the commit.
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
  private record Refused(RemoveItemFromCartResult refusal) implements Change, Refusal {}
}
