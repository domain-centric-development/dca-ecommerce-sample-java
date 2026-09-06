package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.getcartmergeoptions;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
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
   * @param total total cart value
   * @param items list of item summaries
   */
  public record CartSummary(
      String cartId, int itemCount, int totalQuantity, Money total, List<CartItemSummary> items) {}

  /**
   * Summary of a cart item for display.
   *
   * @param productId the product ID
   * @param productName the product name
   * @param imageUrl the product image URL
   * @param quantity the quantity
   * @param unitPrice the unit price
   */
  public record CartItemSummary(
      String productId, String productName, String imageUrl, int quantity, Money unitPrice) {}
}
