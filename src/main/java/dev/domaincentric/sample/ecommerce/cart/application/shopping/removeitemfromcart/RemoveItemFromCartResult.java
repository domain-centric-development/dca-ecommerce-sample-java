package dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * Output model for removing an item from cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the updated list of cart items, each at today's price
 * @param total the total cart amount at today's prices
 */
public record RemoveItemFromCartResult(
    String cartId, String customerId, List<CartItemSummary> items, Money total) {

  /** Builds the result from the cart as it stands after the item was removed, at today's prices. */
  public static RemoveItemFromCartResult from(final EnrichedCart cart) {
    return new RemoveItemFromCartResult(
        cart.cartId().value(),
        cart.customerId().value(),
        cart.items().stream().map(CartItemSummary::from).toList(),
        cart.calculateCurrentSubtotal());
  }
}
