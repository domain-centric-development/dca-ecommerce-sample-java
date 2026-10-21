package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A guest whose cart line already holds the whole stock presses "Add to Cart" once more: the cart
 * refuses the unit and says what it holds and how many are in stock. The test runs a shop of its
 * own, started with nothing but the seed, so the "Wooden Hexagon Desk Model" has its seeded stock
 * of 8.
 */
@DisplayName("A cart line holds no more than the product's stock")
class AddPastTheStockE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Adding past the stock on the page is refused")
  void addingAUnitPastTheStockIsRefusedAndTheLineKeepsItsQuantity() {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("add_past_the_stock_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ProductDetailPage product =
          ProductCatalogPage.navigateTo(page, shopUrl).viewProductNamed(DESK_MODEL);
      final String deskModel = product.shownPath();
      assertEquals(
          "8 units available",
          product.metaValue("Availability"),
          "The desk model starts with its seeded stock");
      for (int i = 0; i < 8; i++) {
        ProductDetailPage.navigateTo(page, shopUrl, deskModel).addToCart();
      }
      assertEquals(List.of(DESK_MODEL + " x8"), CartPage.navigateTo(page, shopUrl).lineItems());

      final CartPage cart = ProductDetailPage.navigateTo(page, shopUrl, deskModel).addToCart();

      assertThat(cart.errorMessage())
          .hasText(
              "There is not enough stock to add 1 of this product — your cart already holds 8,"
                  + " and 8 are in stock.");
      assertEquals(List.of(DESK_MODEL + " x8"), cart.lineItems());
    }
  }
}
