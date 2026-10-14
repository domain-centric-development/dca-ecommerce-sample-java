package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The product page a visitor reaches from a card of the catalogue. A test that reads an exact stock
 * reads a product of its own (see {@link OwnProduct}).
 */
@DisplayName("Product Page E2E Tests")
class ProductPageE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("View Details on a catalogue card opens that product's page")
  void viewDetailsOpensTheProductsPageHeadedWithItsName() {
    ProductDetailPage productPage =
        ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design");

    assertTrue(
        getCurrentPath().matches("/products/[0-9a-f-]{36}"),
        "A product page is open: " + getCurrentPath());
    assertTrue(productPage.isDisplayed(), "The product page renders its details");
    assertEquals("Domain-Driven Design", productPage.heading());
  }

  /**
   * The scenario names the seeded "Domain-Driven Design" with its stock of 20; the test reads a
   * product of its own, created with the same stock, since other tests' orders lower the seeded
   * one.
   */
  @Test
  @DisplayName("Product page shows units available")
  void productPageShowsTheUnitsItWasStockedWith() throws Exception {
    final OwnProduct product = OwnProduct.stocked("Units Available Notebook", 20);
    navigateTo(product.path());
    ProductDetailPage productPage = new ProductDetailPage(page);

    assertEquals("In Stock", productPage.stockBadge());
    assertEquals("20 units available", productPage.metaValue("Availability"));
  }
}
