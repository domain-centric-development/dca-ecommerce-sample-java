package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

/**
 * Enriched Domain Model representing a shopping cart with current article data.
 *
 * <p>This domain concept combines cart state from the ShoppingCart aggregate with external data
 * from the Pricing and Inventory contexts. It owns business logic that requires cross-context data,
 * such as checkout eligibility and price change detection.
 *
 * <p><b>Responsibility Split:</b>
 *
 * <ul>
 *   <li>ShoppingCart aggregate: owns cart mutations (add/remove items, checkout)
 *   <li>EnrichedCart: owns cross-context business rules (validation, calculations)
 * </ul>
 */
public record EnrichedCart(
    CartId cartId, CustomerId customerId, List<EnrichedCartItem> items, CartStatus status)
    implements Value {

  private static final Currency DEFAULT_CURRENCY = Currency.getInstance("EUR");

  /** VAT contained in the gross prices this context works with. */
  private static final BigDecimal RATE = BigDecimal.valueOf(0.19);

  public EnrichedCart {
    if (cartId == null) {
      throw new IllegalArgumentException("Cart ID cannot be null");
    }
    if (customerId == null) {
      throw new IllegalArgumentException("Customer ID cannot be null");
    }
    if (items == null) {
      throw new IllegalArgumentException("Items cannot be null");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status cannot be null");
    }
    // Make defensive copy to ensure immutability
    items = List.copyOf(items);
  }

  /**
   * Creates a new EnrichedCart with the specified values.
   *
   * @param cartId the cart identifier
   * @param customerId the customer identifier
   * @param items the list of enriched cart items
   * @param status the cart status
   * @return a new EnrichedCart instance
   */
  public static EnrichedCart of(
      final CartId cartId,
      final CustomerId customerId,
      final List<EnrichedCartItem> items,
      final CartStatus status) {
    return new EnrichedCart(cartId, customerId, items, status);
  }

  /**
   * Calculates the subtotal using current article prices.
   *
   * @return the sum of all current line totals
   */
  /**
   * The tax contained in the cart's current subtotal, at the cart's rate.
   *
   * <p>Prices are gross, so the tax is <em>contained</em> in the amount rather than added to it.
   * The rule is the cart's own: it taxes goods, while the checkout taxes goods and shipping, so the
   * two contexts state it separately rather than sharing one type.
   */
  public Money containedTax() {
    return containedTax(RATE);
  }

  /** The tax contained in the current subtotal at the given rate. */
  public Money containedTax(final BigDecimal taxRate) {
    if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Tax rate cannot be negative");
    }
    final Money gross = calculateCurrentSubtotal();
    final BigDecimal net =
        gross.amount().divide(BigDecimal.ONE.add(taxRate), 10, RoundingMode.HALF_UP);
    return Money.of(
        gross.amount().subtract(net).setScale(2, RoundingMode.HALF_UP), gross.currency());
  }

  public Money calculateCurrentSubtotal() {
    return items.stream()
        .map(EnrichedCartItem::currentLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }

  /**
   * Calculates the subtotal using original prices from when items were added.
   *
   * @return the sum of all original line totals
   */
  public Money calculateOriginalSubtotal() {
    return items.stream()
        .map(EnrichedCartItem::originalLineTotal)
        .reduce(Money.zero(DEFAULT_CURRENCY), Money::add);
  }

  /**
   * Calculates the total price difference between current and original subtotals.
   *
   * @return the absolute difference between current and original subtotals
   */
  public Money totalPriceDifference() {
    var current = calculateCurrentSubtotal();
    var original = calculateOriginalSubtotal();

    if (current.isGreaterThan(original)) {
      return current.subtract(original);
    } else {
      return original.subtract(current);
    }
  }

  /**
   * Checks if any items have price changes since they were added to the cart.
   *
   * @return true if at least one item has a price change
   */
  public boolean hasAnyPriceChanges() {
    return items.stream().anyMatch(EnrichedCartItem::hasPriceChanged);
  }

  /**
   * Returns all items that have price changes since they were added.
   *
   * @return list of items with price changes
   */
  public List<EnrichedCartItem> itemsWithPriceChanges() {
    return items.stream().filter(EnrichedCartItem::hasPriceChanged).toList();
  }

  /**
   * Checks if all items in the cart are valid for checkout.
   *
   * <p>A cart is valid for checkout if it is active, not empty, and all items are available with
   * sufficient stock.
   *
   * @return true if the cart can proceed to checkout
   */
  public boolean isValidForCheckout() {
    return status == CartStatus.ACTIVE
        && !items.isEmpty()
        && items.stream().allMatch(EnrichedCartItem::isValidForCheckout);
  }

  /**
   * Returns all items that are not valid for checkout.
   *
   * @return list of items that cannot proceed to checkout
   */
  public List<EnrichedCartItem> invalidItems() {
    return items.stream().filter(item -> !item.isValidForCheckout()).toList();
  }
}
