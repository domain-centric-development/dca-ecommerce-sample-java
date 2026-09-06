package dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Response containing cart merge options information.
 *
 * <p>When both anonymous and account carts have items, this response provides details about each
 * cart so the user can make an informed decision about how to handle the merge.
 *
 * @param mergeRequired true if user must choose between merge options
 * @param anonymousCart summary of the anonymous cart (null if empty/not exists)
 * @param accountCart summary of the account cart (null if empty/not exists)
 */
public record GetCartMergeOptionsResult(
    boolean mergeRequired, @Nullable CartSummary anonymousCart, @Nullable CartSummary accountCart) {

  /** Creates a response indicating no merge is required. */
  public static GetCartMergeOptionsResult noMergeRequired() {
    return new GetCartMergeOptionsResult(false, null, null);
  }

  /** Creates a response indicating merge options should be presented. */
  public static GetCartMergeOptionsResult mergeRequired(
      CartSummary anonymousCart, CartSummary accountCart) {
    return new GetCartMergeOptionsResult(true, anonymousCart, accountCart);
  }

  /**
   * Summary of a cart for display in merge options UI.
   *
   * @param cartId the cart ID
   * @param itemCount number of distinct items
   * @param totalQuantity total quantity of all items
   * @param totalAmount total cart value
   * @param totalCurrency currency of total
   * @param items list of item summaries
   */
  public record CartSummary(
      String cartId,
      int itemCount,
      int totalQuantity,
      BigDecimal totalAmount,
      String totalCurrency,
      List<CartItemSummary> items) {}

  /**
   * Summary of a cart item for display.
   *
   * @param productId the product ID
   * @param quantity the quantity
   * @param unitPriceAmount the unit price amount
   * @param unitPriceCurrency the unit price currency
   */
  public record CartItemSummary(
      String productId,
      String productName,
      String imageUrl,
      int quantity,
      BigDecimal unitPriceAmount,
      String unitPriceCurrency) {}
}
