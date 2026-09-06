package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.service.CartTotalCalculator;
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

  /** Creates a CartPageViewModel from an EnrichedCart. */
  public static CartPageViewModel fromEnrichedCart(
      final EnrichedCart cart, final CartTotalCalculator totalCalculator) {
    final var lineItems = cart.items().stream().map(LineItemViewModel::fromEnrichedItem).toList();

    final int totalQuantity = cart.items().stream().mapToInt(item -> item.quantity().value()).sum();

    return new CartPageViewModel(
        cart.cartId().value(),
        cart.status().name(),
        lineItems,
        TotalsViewModel.fromEnrichedCart(cart, totalCalculator),
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
      final var original = item.priceAtAddition().value();
      final var current = item.currentArticle().currentPrice();
      final var diff = item.priceDifference();

      return new PriceChangeViewModel(
          original.amount(),
          current.amount(),
          diff.amount(),
          current.isGreaterThan(original),
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
    static TotalsViewModel fromEnrichedCart(
        final EnrichedCart cart, final CartTotalCalculator totalCalculator) {
      final var current = cart.calculateCurrentSubtotal();
      final var original = cart.calculateOriginalSubtotal();
      final var diff = cart.totalPriceDifference();

      return new TotalsViewModel(
          current.amount(),
          original.amount(),
          diff.amount(),
          totalCalculator.containedTax(current).amount(),
          current.currency().getCurrencyCode());
    }
  }
}
