package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import org.jspecify.annotations.Nullable;

/**
 * Summary of a cart item as the cart use cases report it.
 *
 * <p>A part record shared by several results, which is why it lives in {@code application/shared}
 * rather than in one use-case package. It carries values only — the unit price stays a {@link
 * Money}; the adapters split it into amount and currency code for their formats.
 *
 * @param itemId the cart item ID
 * @param productId the product ID
 * @param quantity the quantity
 * @param unitPrice the unit price; {@code null} for a line whose product the catalogue no longer
 *     knows
 * @param available whether the line can be sold today
 */
public record CartItemSummary(
    String itemId, String productId, int quantity, @Nullable Money unitPrice, boolean available) {

  /**
   * Summarizes an item at the price it was added with. No article is looked at, so nothing says the
   * line cannot be sold: it counts as available.
   */
  public static CartItemSummary from(final CartItem item) {
    return new CartItemSummary(
        item.id().value(),
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value(),
        true);
  }

  /**
   * Summarizes an enriched item at the article's current price — without a price, and not
   * available, when the catalogue no longer knows the product.
   */
  public static CartItemSummary from(final EnrichedCartItem item) {
    return new CartItemSummary(
        item.cartItemId().value(),
        item.productId().value(),
        item.quantity().value(),
        item.currentPrice().orElse(null),
        item.isAvailable());
  }
}
