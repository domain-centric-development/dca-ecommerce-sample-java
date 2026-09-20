package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A SKU names one product. The store claims it the way a unique column does, so a check made before
 * the save cannot be overtaken between the two.
 */
class SkuUniquenessTest {

  private final InMemoryProductRepository products = new InMemoryProductRepository();

  @Test
  @DisplayName("A second product under a taken SKU is refused")
  void aSecondProductUnderTheSameSkuIsRefused() {
    products.save(productWith("SKU-TAKEN"));

    assertThrows(IllegalStateException.class, () -> products.save(productWith("SKU-TAKEN")));
  }

  @Test
  @DisplayName("Saving the same product again keeps its SKU")
  void savingTheSameProductAgainKeepsItsSku() {
    final Product product = products.save(productWith("SKU-OWN"));

    products.save(product);

    assertEquals(product.id(), products.findBySku(SKU.of("SKU-OWN")).orElseThrow().id());
  }

  private static Product productWith(final String sku) {
    return Product.create(
        SKU.of(sku),
        ProductName.of("Thing"),
        ProductDescription.of("A thing"),
        Category.of("Books"),
        ImageUrl.of(""),
        Price.of(Money.euro(10)),
        5);
  }
}
