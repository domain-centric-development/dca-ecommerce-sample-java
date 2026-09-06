package dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts;

import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * Output model for retrieving all shopping carts.
 *
 * @param carts the list of cart summaries
 */
public record GetAllCartsResult(List<CartSummary> carts) {

  /** Summarizes every cart of the list. */
  public static GetAllCartsResult from(final List<ShoppingCart> carts) {
    return new GetAllCartsResult(carts.stream().map(CartSummary::from).toList());
  }

  /**
   * Summary of a shopping cart.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param status the cart status
   * @param itemCount the number of items in the cart
   * @param total the total amount
   */
  public record CartSummary(
      String cartId, String customerId, String status, int itemCount, Money total) {

    static CartSummary from(final ShoppingCart cart) {
      return new CartSummary(
          cart.id().value(),
          cart.customerId().value(),
          cart.status().name(),
          cart.items().size(),
          cart.calculateTotal());
    }
  }
}
