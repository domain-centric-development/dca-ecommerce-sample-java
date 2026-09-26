package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The catalogue page names itself in the browser tab the same way in both samples. */
@DisplayName("Catalog Title E2E Tests")
class CatalogTitleE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("The catalogue page is titled Product Catalog")
  void catalogPageIsTitledProductCatalog() {
    ProductCatalogPage.navigateTo(page);

    assertEquals("Product Catalog", page.title());
  }
}
