package dev.domaincentric.sample.ecommerce.cart.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuantityTest {

  @Test
  @DisplayName("a quantity is never zero or less")
  void quantityIsNeverZeroOrLess() {
    assertThrows(IllegalArgumentException.class, () -> Quantity.of(0));
    assertThrows(IllegalArgumentException.class, () -> Quantity.of(-1));
  }

  @Test
  @DisplayName("adding a quantity gives the sum")
  void addingGivesTheSum() {
    assertEquals(Quantity.of(5), Quantity.of(3).add(Quantity.of(2)));
  }

  @Test
  @DisplayName("subtracting a smaller quantity leaves the rest")
  void subtractingLeavesTheRest() {
    assertEquals(Quantity.of(1), Quantity.of(3).subtract(Quantity.of(2)));
  }

  @Test
  @DisplayName("subtracting the whole quantity is refused, nothing may be left")
  void subtractingTheWholeQuantityIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> Quantity.of(3).subtract(Quantity.of(3)));
  }

  @Test
  @DisplayName("subtracting more than there is is refused")
  void subtractingMoreIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> Quantity.of(2).subtract(Quantity.of(3)));
  }

  @Test
  @DisplayName("increase adds one, decrease takes one away but never the last")
  void increaseAndDecrease() {
    assertEquals(Quantity.of(3), Quantity.of(2).increase());
    assertEquals(Quantity.of(1), Quantity.of(2).decrease());
    assertThrows(IllegalArgumentException.class, () -> Quantity.of(1).decrease());
  }
}
