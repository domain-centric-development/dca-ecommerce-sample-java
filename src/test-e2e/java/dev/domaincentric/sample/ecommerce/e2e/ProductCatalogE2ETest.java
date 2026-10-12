package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** What a card of the seeded catalogue shows a visitor. */
@DisplayName("Product Catalog E2E Tests")
class ProductCatalogE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Card shows the price")
  void cardShowsThePriceTheProductWasCreatedWith() {
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);

    assertEquals("54.99 EUR", catalog.cardPriceOf("Domain-Driven Design"));
  }
}
