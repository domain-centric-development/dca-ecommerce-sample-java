package dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * Output model for retrieving all shopping carts.
 *
 * @param carts the list of cart summaries
 */
public record GetAllCartsResult(List<CartSummary> carts) {

  /** Summarizes every cart of the list. */
  public static GetAllCartsResult from(final List<EnrichedCart> carts) {
    return new GetAllCartsResult(carts.stream().map(CartSummary::from).toList());
  }

  /**
   * Summary of a shopping cart.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param status the cart status
   * @param itemCount the number of items in the cart
   * @param total the total amount at today's prices
   */
  public record CartSummary(
      String cartId, String customerId, CartStatus status, int itemCount, Money total) {

    static CartSummary from(final EnrichedCart cart) {
      return new CartSummary(
          cart.cartId().value(),
          cart.customerId().value(),
          cart.status(),
          cart.items().size(),
          cart.calculateCurrentSubtotal());
    }
  }
}
