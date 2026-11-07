package dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItemId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.List;

/**
 * What taking a line out of a cart came to: the line was removed, or one of the refusals a caller
 * answers.
 */
public sealed interface RemoveCartItemResult
    permits RemoveCartItemResult.Removed,
        RemoveCartItemResult.CartNotFound,
        RemoveCartItemResult.CartItemNotFound,
        RemoveCartItemResult.CartNotModifiable {

  /**
   * The line was removed.
   *
   * @param cartId the cart ID
   * @param items the lines the cart holds after the removal, in their order
   */
  record Removed(String cartId, List<CartItemSummary> items) implements RemoveCartItemResult {

    /** Builds the case from the cart as it stands after the line was removed. */
    public static Removed from(final ShoppingCart cart) {
      return new Removed(
          cart.id().value(), cart.items().stream().map(CartItemSummary::from).toList());
    }
  }

  /**
   * No cart of this customer under that identity (ADR-036).
   *
   * @param cartId the cart the caller named
   */
  record CartNotFound(CartId cartId) implements RemoveCartItemResult, Refusal {}

  /**
   * The cart holds no line with that identity — the aggregate's rule.
   *
   * @param itemId the line the caller named
   */
  record CartItemNotFound(CartItemId itemId) implements RemoveCartItemResult, Refusal {}

  /**
   * The cart is no longer active and gives no line up — the aggregate's rule.
   *
   * @param cartId the cart
   * @param status the status that refuses the change
   */
  record CartNotModifiable(CartId cartId, CartStatus status)
      implements RemoveCartItemResult, Refusal {}
}
