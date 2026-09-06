package dev.domaincentric.sample.ecommerce.cart.application.removeitemfromcart;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output model for removing an item from cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param items the updated list of cart items
 * @param totalAmount the total cart amount
 * @param totalCurrency the total cart currency
 */
public record RemoveItemFromCartResult(
    String cartId,
    String customerId,
    List<CartItemSummary> items,
    BigDecimal totalAmount,
    String totalCurrency) {

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
