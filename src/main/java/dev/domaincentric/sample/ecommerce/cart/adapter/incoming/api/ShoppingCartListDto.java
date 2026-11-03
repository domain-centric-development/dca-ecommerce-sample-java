package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for list of shopping carts.
 *
 * @param carts the list of cart summaries
 */
public record ShoppingCartListDto(List<CartSummaryDto> carts) {

  /**
   * Summary of a shopping cart.
   *
   * @param cartId the cart ID
   * @param customerId the customer ID
   * @param status the cart status
   * @param itemCount the number of items in the cart
   * @param total the total amount
   * @param currency the total currency
   * @param items the cart's lines
   */
  public record CartSummaryDto(
      String cartId,
      String customerId,
      String status,
      int itemCount,
      BigDecimal total,
      String currency,
      List<CartItemDto> items) {}
}
