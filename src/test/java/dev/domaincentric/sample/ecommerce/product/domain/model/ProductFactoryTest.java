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
            factory.createProduct(
                SKU.of("SPEC-1"),
                ProductName.of("Specification item"),
                dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription.empty(),
                Category.of("Test"),
                dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl.empty(),
                dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(
                    Money.euro(1)),
                -1));
    assertNotNull(
        factory.createProduct(
            SKU.of("SPEC-2"),
            ProductName.of("Specification item"),
            dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription.empty(),
            Category.of("Test"),
            dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl.empty(),
            dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price.of(Money.euro(1)),
            0));
  }
}
