package dev.domaincentric.sample.ecommerce.cart.application.getallcarts;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output model for retrieving all shopping carts.
 *
 * @param carts the list of cart summaries
 */
public record GetAllCartsResult(List<CartSummary> carts) {

  /**
   * Summary of a shopping cart.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param status the cart status
   * @param itemCount the number of items in the cart
   * @param totalAmount the total amount
   * @param totalCurrency the total currency
   */
  public record CartSummary(
      String cartId,
      String customerId,
      String status,
      int itemCount,
      BigDecimal totalAmount,
      String totalCurrency) {}
}
