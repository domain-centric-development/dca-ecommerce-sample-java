package dev.domaincentric.sample.ecommerce.cart.application.checkoutcart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Output model for cart checkout.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the cart items at checkout
 * @param totalAmount the total amount
 * @param totalCurrency the total currency
 * @param checkedOutAt the checkout timestamp
 */
public record CheckoutCartResult(
    String cartId,
    String customerId,
    List<CartItemSummary> items,
    BigDecimal totalAmount,
    String totalCurrency,
    Instant checkedOutAt) {

  /**
   * Summary of a cart item.
   *
   * @param itemId the cart item ID
   * @param productId the product ID
   * @param quantity the quantity
   * @param unitPriceAmount the unit price amount
   * @param unitPriceCurrency the unit price currency
   */
  public record CartItemSummary(
      String itemId,
      String productId,
      int quantity,
      BigDecimal unitPriceAmount,
      String unitPriceCurrency) {}
}
