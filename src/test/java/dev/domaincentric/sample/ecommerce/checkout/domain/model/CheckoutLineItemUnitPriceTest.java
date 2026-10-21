package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutLineItem: the same line at another unit price")
class CheckoutLineItemUnitPriceTest {

  private final CheckoutLineItem line =
      new CheckoutLineItem(
          CheckoutLineItemId.generate(),
          ProductId.generate(),
          "Domain-Driven Design",
          Money.euro(54.99),
          2,
          "/images/ddd.png",
          "position-1");

  @Test
  @DisplayName("keeps id, product, name, quantity, image and position, with the new price")
  void keepsEverythingButThePrice() {
    final CheckoutLineItem repriced = line.withUnitPrice(Money.euro(49.99));

    assertEquals(
        new CheckoutLineItem(
            line.id(),
            line.productId(),
            "Domain-Driven Design",
            Money.euro(49.99),
            2,
            "/images/ddd.png",
            "position-1"),
        repriced);
  }

  @Test
  @DisplayName("refuses a missing price")
  void refusesAMissingPrice() {
    assertThrows(IllegalArgumentException.class, () -> line.withUnitPrice(null));
  }
}
