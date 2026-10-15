package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Price")
class PriceTest {

  private static final Currency EUR = Currency.getInstance("EUR");

  @Test
  @DisplayName("refuses an amount of zero and accepts one cent")
  void refusesZeroAndAcceptsOneCent() {
    assertThrows(IllegalArgumentException.class, () -> Price.of(Money.of(BigDecimal.ZERO, EUR)));

    final Price oneCent = Price.of(Money.of(new BigDecimal("0.01"), EUR));

    assertEquals(new BigDecimal("0.01"), oneCent.value().amount());
  }
}
