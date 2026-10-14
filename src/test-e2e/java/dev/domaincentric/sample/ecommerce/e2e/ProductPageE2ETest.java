package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The product page a visitor reaches from a card of the catalogue. A test that reads a seeded stock
 * exactly runs a shop of its own: orders in the suite's shop lower that stock.
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
   * The scenario names the seeded "Domain-Driven Design" with its stock of 20; the shop is one of
   * its own, started with nothing but the seed.
   */
  @Test
  @DisplayName("Product page shows units available")
  void productPageShowsTheUnitsItWasStockedWith() {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("units_available_e2e")) {
      ProductDetailPage productPage =
          ProductCatalogPage.navigateTo(page, ShopUnderTest.baseUrlOf(shop))
              .viewProductNamed("Domain-Driven Design");

      assertEquals("In Stock", productPage.stockBadge());
      assertEquals("20 units available", productPage.metaValue("Availability"));
    }
  }
}
