package dev.domaincentric.sample.ecommerce.cart.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart.CompleteCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart.CompleteCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdQuery;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.GetCartByIdResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Shopping Cart.
 *
 * <p>Exposes cart snapshots and cart completion for cross-module access. The Checkout context uses
 * this service instead of directly accessing Cart internals (repository, domain model).
 *
 * <p><b>Hexagonal Architecture:</b> As an incoming adapter, this service calls input ports (use
 * cases), NOT output ports (repositories) directly.
 */
@OpenHostService(
    context = "Shopping Cart",
    description =
        "Provides cart data and cart completion for other bounded contexts (primarily Checkout)")
@Service
public class CartService {

  private final GetCartByIdInputPort getCartByIdInputPort;
  private final CompleteCartInputPort completeCartInputPort;

  public CartService(
      GetCartByIdInputPort getCartByIdInputPort, CompleteCartInputPort completeCartInputPort) {
    this.getCartByIdInputPort = getCartByIdInputPort;
    this.completeCartInputPort = completeCartInputPort;
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
  public record CartItemSnapshot(ProductId productId, Price priceAtAddition, int quantity) {}

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
    GetCartByIdResult result =
        getCartByIdInputPort.execute(new GetCartByIdQuery(cartId.toString(), customerId));

    if (!result.found()) {
      return Optional.empty();
    }

    var enrichedCart = result.cart().orElseThrow();

    List<CartItemSnapshot> items =
        enrichedCart.items().stream()
            .map(
                item ->
                    new CartItemSnapshot(
                        item.productId(), item.priceAtAddition(), item.quantity().value()))
            .toList();

    return Optional.of(
        new CartSnapshot(
            UUID.fromString(enrichedCart.cartId().value()),
            enrichedCart.customerId().value(),
            items,
            enrichedCart.status() == CartStatus.ACTIVE));
  }

  /**
   * Marks a cart as completed after checkout confirmation.
   *
   * <p>Unscoped on purpose: this one acts on nobody's behalf — it is the system reacting to its own
   * event, delivered at least once, with no caller to check.
   *
   * @param cartId the cart ID to complete
   */
  public void completeCart(UUID cartId) {
    completeCartInputPort.execute(new CompleteCartCommand(cartId.toString()));
  }
}
