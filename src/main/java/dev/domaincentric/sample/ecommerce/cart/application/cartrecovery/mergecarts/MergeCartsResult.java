package dev.domaincentric.sample.ecommerce.cart.application.mergecarts;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response from the cart merge operation.
 *
 * @param cartId the resulting cart ID (account cart)
 * @param customerId the registered user's customer ID
 * @param items the final list of cart items
 * @param totalAmount the total cart amount
 * @param totalCurrency the total cart currency
 * @param strategyApplied the strategy that was applied
 * @param itemsFromAnonymous number of items taken from anonymous cart
 * @param itemsFromAccount number of items kept from account cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record MergeCartsResult(
    String cartId,
    String customerId,
    List<CartItemSummary> items,
    BigDecimal totalAmount,
    String totalCurrency,
    CartMergeStrategy strategyApplied,
    int itemsFromAnonymous,
    int itemsFromAccount,
    boolean anonymousCartDeleted) {

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
