package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions.GetCartMergeOptionsResult;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the cart merge options page.
 *
 * <p>Contains data needed to render the merge options UI when a user logs in and has items in both
 * their anonymous cart and account cart.
 */
public record CartMergePageViewModel(
    CartSummaryViewModel anonymousCart,
    CartSummaryViewModel accountCart,
    String anonymousUserId,
    @Nullable String returnUrl) {

  /** Creates a CartMergePageViewModel from GetCartMergeOptionsResult. */
  public static CartMergePageViewModel fromResult(
      final GetCartMergeOptionsResult result,
      final String anonymousUserId,
      final @Nullable String returnUrl) {
    return new CartMergePageViewModel(
        CartSummaryViewModel.fromSummary(result.anonymousCart()),
        CartSummaryViewModel.fromSummary(result.accountCart()),
        anonymousUserId,
        returnUrl);
  }

  /** Cart summary for display. */
  public record CartSummaryViewModel(
      String cartId,
      int itemCount,
      int totalQuantity,
      BigDecimal totalAmount,
      String currencyCode,
      List<CartItemViewModel> items) {
    static CartSummaryViewModel fromSummary(final GetCartMergeOptionsResult.CartSummary summary) {
      if (summary == null) {
        return null;
      }
      return new CartSummaryViewModel(
          summary.cartId(),
          summary.itemCount(),
          summary.totalQuantity(),
          summary.totalAmount(),
          summary.totalCurrency(),
          summary.items().stream().map(CartItemViewModel::fromItemSummary).toList());
    }
  }

  /** Cart item for display. */
  public record CartItemViewModel(
      String productId,
      String productName,
      String imageUrl,
      int quantity,
      BigDecimal unitPrice,
      String currencyCode) {
    static CartItemViewModel fromItemSummary(final GetCartMergeOptionsResult.CartItemSummary item) {
      return new CartItemViewModel(
          item.productId(),
          item.productName(),
          item.imageUrl(),
          item.quantity(),
          item.unitPriceAmount(),
          item.unitPriceCurrency());
    }
  }
}
