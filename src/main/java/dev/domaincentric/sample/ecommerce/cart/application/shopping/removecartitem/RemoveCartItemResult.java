package dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.List;

/**
 * Output model for taking a line out of a cart.
 *
 * @param cartId the cart ID
 * @param items the lines the cart holds after the removal, in their order
 */
public record RemoveCartItemResult(String cartId, List<CartItemSummary> items) {

  /** Builds the result from the cart as it stands after the line was removed. */
  public static RemoveCartItemResult from(final ShoppingCart cart) {
    return new RemoveCartItemResult(
        cart.id().value(), cart.items().stream().map(CartItemSummary::from).toList());
  }
}
