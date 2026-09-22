package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Value Object representing the calculated totals for a checkout session.
 *
 * <p>Contains subtotal, shipping, the contained tax and the grand total. Prices are gross prices:
 * {@code tax} is the share of {@code subtotal} and {@code shipping} that is value-added tax, not an
 * additional charge — which is why {@code total} is subtotal plus shipping and does not include it
 * a second time.
 */
public record CheckoutTotals(Money subtotal, Money shipping, Money tax, Money total)
    implements Value {

  /** VAT contained in the gross amounts this context works with. */
  private static final BigDecimal RATE = BigDecimal.valueOf(0.19);

  public CheckoutTotals {
    if (subtotal == null) {
      throw new IllegalArgumentException("Subtotal cannot be null");
    }
    if (shipping == null) {
      throw new IllegalArgumentException("Shipping cannot be null");
    }
    if (tax == null) {
      throw new IllegalArgumentException("Tax cannot be null");
    }
    if (total == null) {
      throw new IllegalArgumentException("Total cannot be null");
    }
  }

  public static CheckoutTotals of(
      final Money subtotal, final Money shipping, final Money tax, final Money total) {
    return new CheckoutTotals(subtotal, shipping, tax, total);
  }

  public static CheckoutTotals calculate(
      final Money subtotal, final Money shipping, final Money tax) {
    var total = subtotal.add(shipping);
    return new CheckoutTotals(subtotal, shipping, tax, total);
  }

  /**
   * Totals for goods and shipping, with the contained tax derived from them at the context's rate.
   *
   * <p>The rate lives here because the rule is the checkout's own: prices are gross, so the tax is
   * <em>contained</em> in the amount rather than added to it, and the grand total does not change
   * when it is worked out. A context that taxes a different basis — the cart taxes goods only —
   * states its own rule in its own type instead of sharing this one.
   */
  public static CheckoutTotals calculate(final Money subtotal, final Money shipping) {
    return calculate(subtotal, shipping, containedTax(subtotal.add(shipping)));
  }

  /** The tax contained in a gross amount at the checkout's rate. */
  public static Money containedTax(final Money grossAmount) {
    return containedTax(grossAmount, RATE);
  }

  /** The tax contained in a gross amount at the given rate. */
  public static Money containedTax(final Money grossAmount, final BigDecimal taxRate) {
    if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Tax rate cannot be negative");
    }
    final BigDecimal net =
        grossAmount.amount().divide(BigDecimal.ONE.add(taxRate), 10, RoundingMode.HALF_UP);
    final BigDecimal tax = grossAmount.amount().subtract(net);
    return Money.of(tax.setScale(2, RoundingMode.HALF_UP), grossAmount.currency());
  }

  public static CheckoutTotals zero(final Currency currency) {
    var zero = Money.zero(currency);
    return new CheckoutTotals(zero, zero, zero, zero);
  }

  /** Shipping changes the basis, so the contained tax is worked out again. */
  public CheckoutTotals withShipping(final Money newShipping) {
    return CheckoutTotals.calculate(this.subtotal, newShipping);
  }

  public CheckoutTotals withTax(final Money newTax) {
    return CheckoutTotals.calculate(this.subtotal, this.shipping, newTax);
  }
}
