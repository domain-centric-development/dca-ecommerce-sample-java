package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The product page a visitor reaches from a card of the seeded catalogue. */
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
}
