package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for what a ShoppingCart reports about its lines: its total and its item count. */
@DisplayName("ShoppingCart totals")
class ShoppingCartTotalsTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  private final ShoppingCart cart =
      ShoppingCart.create(CartId.generate(), CustomerId.of("customer"));

  @Test
  @DisplayName("the total is each line's stored price times its quantity, summed")
  void totalSumsEachLinesStoredPriceTimesItsQuantity() {
    cart.addItem(ProductId.generate(), Quantity.of(1), euro(10.00));
    cart.addItem(ProductId.generate(), Quantity.of(2), euro(25.00));

    assertEquals(Money.of(BigDecimal.valueOf(60.00), EUR), cart.calculateTotal());
  }

  @Test
  @DisplayName("the item count counts lines, not units")
  void itemCountCountsLinesNotUnits() {
    cart.addItem(ProductId.generate(), Quantity.of(3), euro(10.00));
    cart.addItem(ProductId.generate(), Quantity.of(2), euro(10.00));

    assertEquals(2, cart.itemCount());
  }

  private static Price euro(double amount) {
    return Price.of(Money.of(BigDecimal.valueOf(amount), EUR));
  }
}
