package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web.shopping;

import dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid.CartTotals;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import java.math.BigDecimal;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * ViewModel for the shopping cart page.
 *
 * <p>Contains all data needed to render the cart view including line items, totals, price change
 * alerts, and checkout eligibility.
 */
public record CartPageViewModel(
    String cartId,
    String status,
    List<LineItemViewModel> lineItems,
    TotalsViewModel totals,
    int itemCount,
    int totalQuantity,
    boolean hasAnyPriceChanges,
    boolean canCheckout) {

  /** Creates a CartPageViewModel from an EnrichedCart and the totals the use case assembled. */
  public static CartPageViewModel fromEnrichedCart(
      final EnrichedCart cart, final CartTotals totals) {
    final var lineItems = cart.items().stream().map(LineItemViewModel::fromEnrichedItem).toList();

    final int totalQuantity = cart.items().stream().mapToInt(item -> item.quantity().value()).sum();

    return new CartPageViewModel(
        cart.cartId().value(),
        cart.status().name(),
        lineItems,
        TotalsViewModel.from(totals),
        cart.items().size(),
        totalQuantity,
        cart.hasAnyPriceChanges(),
        cart.isValidForCheckout());
  }

  /** Checks if the cart is empty. */
  public boolean isEmpty() {
    return lineItems.isEmpty();
  }

  /** Line item for cart display. */
  public record LineItemViewModel(
      String itemId,
      String productId,
      String productName,
      String imageUrl,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal lineTotal,
      String currencyCode,
      boolean hasPriceChanged,
      @Nullable PriceChangeViewModel priceChange,
      boolean hasSufficientStock,
      boolean isAvailable) {
    static LineItemViewModel fromEnrichedItem(final EnrichedCartItem item) {
      final var currentPrice = item.currentArticle().currentPrice();
      final var priceChange =
          item.hasPriceChanged() ? PriceChangeViewModel.fromEnrichedItem(item) : null;

      return new LineItemViewModel(
          item.cartItemId().value(),
          item.productId().value(),
          item.currentArticle().name(),
          item.currentArticle().imageUrl(),
          item.quantity().value(),
          currentPrice.amount(),
          item.currentLineTotal().amount(),
          currentPrice.currency().getCurrencyCode(),
          item.hasPriceChanged(),
          priceChange,
          item.hasSufficientStock(),
          item.currentArticle().isAvailable());
    }
  }

  /** Price change information for display. */
  public record PriceChangeViewModel(
      BigDecimal originalPrice,
      BigDecimal currentPrice,
      BigDecimal difference,
      boolean increased,
      String currencyCode) {
    static PriceChangeViewModel fromEnrichedItem(final EnrichedCartItem item) {
      final var current = item.currentArticle().currentPrice();

      return new PriceChangeViewModel(
          item.priceAtAddition().value().amount(),
          current.amount(),
          item.priceDifference().amount(),
          item.priceIncreased(),
          current.currency().getCurrencyCode());
    }
  }

  /** Cart totals for display. */
  public record TotalsViewModel(
      BigDecimal currentSubtotal,
      BigDecimal originalSubtotal,
      BigDecimal totalDifference,
      BigDecimal containedTax,
      String currencyCode) {
    static TotalsViewModel from(final CartTotals totals) {
      return new TotalsViewModel(
          totals.currentSubtotal().amount(),
          totals.originalSubtotal().amount(),
          totals.difference().amount(),
          totals.containedTax().amount(),
          totals.currentSubtotal().currency().getCurrencyCode());
    }
  }
}
