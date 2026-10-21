package dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ArticleDataPort;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartFactory;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
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
    final ShoppingCart cart =
        transactionBoundary.inTransaction(
            () -> {
              final ShoppingCart loaded =
                  shoppingCartRepository
                      .findByIdForCustomer(cartId, CustomerId.of(input.customerId()))
                      .orElseThrow(() -> new CartNotFoundException(cartId));
              loaded.removeItemByProductId(productId);
              shoppingCartRepository.save(loaded);
              eventPublisher.publishAndClearEvents(loaded);
              return loaded;
            });

    return RemoveItemFromCartResult.from(atTodaysPrices(cart));
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
}
