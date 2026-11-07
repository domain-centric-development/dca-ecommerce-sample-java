package dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * What removing a product from a cart came to: the product was removed, or one of the refusals a
 * caller answers.
 */
public sealed interface RemoveItemFromCartResult
    permits RemoveItemFromCartResult.Removed,
        RemoveItemFromCartResult.CartNotFound,
        RemoveItemFromCartResult.CartItemNotFound,
        RemoveItemFromCartResult.CartNotModifiable {

  /**
   * The product was removed.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param items the updated list of cart items, each at today's price
   * @param total the total cart amount at today's prices
   */
  record Removed(String cartId, String customerId, List<CartItemSummary> items, Money total)
      implements RemoveItemFromCartResult {

    /** Builds the case from the cart as it stands after the item was removed, at today's prices. */
    public static Removed from(final EnrichedCart cart) {
      return new Removed(
          cart.cartId().value(),
          cart.customerId().value(),
          cart.items().stream().map(CartItemSummary::from).toList(),
          cart.calculateCurrentSubtotal());
    }
  }

  /**
   * No cart of this customer under that identity (ADR-036).
   *
   * @param cartId the cart the caller named
   */
  record CartNotFound(CartId cartId) implements RemoveItemFromCartResult, Refusal {}

  /**
   * The cart holds no line for that product — the aggregate's rule.
   *
   * @param productId the product the caller named
   */
  record CartItemNotFound(ProductId productId) implements RemoveItemFromCartResult, Refusal {}

  /**
   * The cart is no longer active and gives no product up — the aggregate's rule.
   *
   * @param cartId the cart
   * @param status the status that refuses the change
   */
  record CartNotModifiable(CartId cartId, CartStatus status)
      implements RemoveItemFromCartResult, Refusal {}
}
