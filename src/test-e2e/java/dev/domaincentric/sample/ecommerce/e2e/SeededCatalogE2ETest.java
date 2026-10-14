package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The sample catalog the shop seeds at start-up: the same 21 products in both samples, listed by
 * product name in ordinal order. Same scenario as the .NET sample's {@code SeededCatalogE2eTest}.
 * The shop is one of its own, started with nothing but the seed: the shop the suite drives also
 * holds the products other tests create.
 */
@DisplayName("Seeded Catalog E2E Tests")
class SeededCatalogE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("The catalog lists the seeded products in name order")
  void catalogListsTheSeededProductsInNameOrder() {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("seeded_catalog_e2e")) {
      List<String> titles =
          ProductCatalogPage.navigateTo(page, ShopUnderTest.baseUrlOf(shop)).productTitles();

      assertEquals(21, titles.size(), "The seed creates 21 products: " + titles);
      assertEquals(titles.stream().sorted().toList(), titles, "Cards are in ordinal name order");
      assertEquals("\"Bounded Context\" Enamel Pin", titles.get(0), "The quoted names sort first");
    }
  }
}
