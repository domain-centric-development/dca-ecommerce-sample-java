package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.NotFoundPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The page a visitor sees on following the address of a product the shop does not have. */
@DisplayName("Product Not Found E2E Tests")
class ProductNotFoundE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Unknown product shows the not-found page")
  void unknownProductAddressShowsTheNotFoundPage() {
    NotFoundPage notFound = NotFoundPage.openProduct(page, "no-such-product");

    assertEquals("404", notFound.code());
    assertEquals("Page Not Found", notFound.heading());
    assertEquals(
        "The page you're looking for doesn't exist or has been removed. Perhaps you were looking"
            + " for one of our products?",
        notFound.message());
    assertEquals("domaincentric.commerce", page.title());
  }
}
