package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * Output model for adding an item to cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the updated list of cart items
 * @param total the total cart amount
 */
public record AddItemToCartResult(
    String cartId, String customerId, List<CartItemSummary> items, Money total) {

  /** Builds the result from the cart as it stands after the item was added. */
  public static AddItemToCartResult from(final ShoppingCart cart) {
    return new AddItemToCartResult(
        cart.id().value(),
        cart.customerId().value(),
        cart.items().stream().map(CartItemSummary::from).toList(),
        cart.calculateTotal());
  }
}
