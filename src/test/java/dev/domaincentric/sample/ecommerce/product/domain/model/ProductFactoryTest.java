package dev.domaincentric.sample.ecommerce.product.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import org.junit.jupiter.api.Test;

class ProductFactoryTest {
  @Test
  void rejectsNegativeInitialStockAndAcceptsZero() {
    var factory = new ProductFactory();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            factory.createBasicProduct(
                SKU.of("SPEC-1"),
                ProductName.of("Specification item"),
                Category.of("Test"),
                dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                    Money.euro(1)),
                -1));
    assertNotNull(
        factory.createBasicProduct(
            SKU.of("SPEC-2"),
            ProductName.of("Specification item"),
            Category.of("Test"),
            dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(Money.euro(1)),
            0));
  }
}
