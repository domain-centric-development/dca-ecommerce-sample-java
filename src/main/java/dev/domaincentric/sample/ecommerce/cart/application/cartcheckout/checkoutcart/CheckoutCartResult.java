package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.time.Instant;
import java.util.List;

/**
 * Output model for cart checkout.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the cart items at checkout, at their current prices
 * @param total the total amount at current prices
 * @param checkedOutAt the checkout timestamp
 */
public record CheckoutCartResult(
    String cartId,
    String customerId,
    List<CartItemSummary> items,
    Money total,
    Instant checkedOutAt) {

  /**
   * Builds the result from the checked-out cart and its enriched view, which carries the current
   * article prices the total was validated against.
   */
  public static CheckoutCartResult from(
      final ShoppingCart cart, final EnrichedCart enrichedCart, final Instant checkedOutAt) {
    return new CheckoutCartResult(
        cart.id().value(),
        enrichedCart.customerId().value(),
        enrichedCart.items().stream().map(CartItemSummary::from).toList(),
        enrichedCart.calculateCurrentSubtotal(),
        checkedOutAt);
  }
}
