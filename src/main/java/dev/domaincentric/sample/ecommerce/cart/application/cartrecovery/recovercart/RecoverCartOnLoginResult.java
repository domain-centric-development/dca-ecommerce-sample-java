package dev.domaincentric.sample.ecommerce.cart.application.recovercart;

import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Output model for recovering cart on login.
 *
 * @param cartId the resulting cart ID (registered user's cart)
 * @param customerId the registered user's customer ID
 * @param items the merged list of cart items
 * @param totalAmount the total cart amount after merge
 * @param totalCurrency the total cart currency
 * @param itemsMerged number of items merged from anonymous cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record RecoverCartOnLoginResult(
    @Nullable String cartId,
    String customerId,
    List<CartItemSummary> items,
    BigDecimal totalAmount,
    String totalCurrency,
    int itemsMerged,
    boolean anonymousCartDeleted) {

  /** Creates a response when no cart recovery was needed (no anonymous cart existed). */
  public static RecoverCartOnLoginResult noRecoveryNeeded(String customerId) {
    return new RecoverCartOnLoginResult(
        null, customerId, List.of(), BigDecimal.ZERO, "EUR", 0, false);
  }

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
