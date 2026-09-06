package dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid;

import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.service.CartTotalCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;

/**
 * The cart's amounts as the page shows them, assembled by the use case so that no adapter runs the
 * domain's calculations.
 *
 * @param currentSubtotal the subtotal at today's prices
 * @param originalSubtotal the subtotal at the prices the items were added with
 * @param difference current minus original subtotal
 * @param containedTax the value-added tax contained in the current subtotal
 */
public record CartTotals(
    Money currentSubtotal, Money originalSubtotal, Money difference, Money containedTax) {

  public static CartTotals from(final EnrichedCart cart, final CartTotalCalculator calculator) {
    final Money current = cart.calculateCurrentSubtotal();
    return new CartTotals(
        current,
        cart.calculateOriginalSubtotal(),
        cart.totalPriceDifference(),
        calculator.containedTax(current));
  }
}
