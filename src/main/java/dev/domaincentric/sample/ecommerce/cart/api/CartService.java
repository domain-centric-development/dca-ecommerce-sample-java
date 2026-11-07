package dev.domaincentric.sample.ecommerce.cart.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart.GetActiveCartQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Shopping Cart.
 *
 * <p>Exposes cart snapshots for cross-module access. Cart completion is no call here: Cart
 * completes a cart when the confirmed checkout's integration event arrives. The Checkout context
 * uses this service instead of directly accessing Cart internals (repository, domain model).
 *
 * <p><b>Hexagonal Architecture:</b> Like an incoming adapter, this service calls input ports (use
 * cases), never output ports (repositories) directly.
 */
@OpenHostService(
    context = "Shopping Cart",
    description = "Provides cart data for other bounded contexts (primarily Checkout)")
@Service
public class CartService {

  private final GetCartByIdInputPort getCartByIdInputPort;
  private final GetActiveCartInputPort getActiveCartInputPort;

  public CartService(
      GetCartByIdInputPort getCartByIdInputPort, GetActiveCartInputPort getActiveCartInputPort) {
    this.getCartByIdInputPort = getCartByIdInputPort;
    this.getActiveCartInputPort = getActiveCartInputPort;
  }

  /**
   * The ID of the customer's active cart — the cart that belongs to that customer now, a guest cart
   * the account took over at login included.
   *
   * @param customerId the customer the calling context is acting for
   * @return the active cart's ID, empty when the customer has none
   */
  public Optional<UUID> findActiveCartId(String customerId) {
    return getActiveCartInputPort
        .execute(new GetActiveCartQuery(customerId))
        .cartId()
        .map(UUID::fromString);
  }

  /**
   * Snapshot of a shopping cart for cross-context communication.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param items the cart items
   * @param active whether the cart is active
   */
  public record CartSnapshot(
      UUID cartId, String customerId, List<CartItemSnapshot> items, boolean active) {}

  /**
   * Snapshot of a cart item for cross-context communication.
   *
   * @param productId the product ID
   * @param priceAtAddition the price when item was added
   * @param quantity the item quantity
   */
  public record CartItemSnapshot(
      ProductId productId, Price priceAtAddition, int quantity, String positionSnapshot) {}

  /**
   * Retrieves cart data by ID.
   *
   * <p>Empty when the cart does not exist <em>or</em> is not that customer's. A consuming context
   * has to name the customer it is acting for; that is what keeps the ownership rule in the context
   * that owns carts instead of in every caller.
   *
   * @param cartId the cart ID
   * @param customerId the customer the calling context is acting for
   * @return cart snapshot if found
   */
  public Optional<CartSnapshot> findCartById(UUID cartId, String customerId) {
    return switch (getCartByIdInputPort.execute(
        new GetCartByIdQuery(cartId.toString(), customerId))) {
      case GetCartByIdResult.Found found -> Optional.of(snapshotOf(found.cart()));
      case GetCartByIdResult.CartNotFound refusal -> Optional.empty();
    };
  }

  private static CartSnapshot snapshotOf(final EnrichedCart enrichedCart) {

    List<CartItemSnapshot> items =
        enrichedCart.items().stream()
            .map(
                item ->
                    new CartItemSnapshot(
                        item.productId(),
                        item.priceAtAddition(),
                        item.quantity().value(),
                        item.positionSnapshot()))
            .toList();

    return new CartSnapshot(
        UUID.fromString(enrichedCart.cartId().value()),
        enrichedCart.customerId().value(),
        items,
        enrichedCart.status() == CartStatus.ACTIVE);
  }
}
