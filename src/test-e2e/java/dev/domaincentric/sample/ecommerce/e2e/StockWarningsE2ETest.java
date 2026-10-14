package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A guest's cart line holds more than another guest's order left in stock: the cart page tells the
 * first guest on that line, before they try to check out. The test runs a shop of its own, started
 * with nothing but the seed: the order it arranges uses up the seeded stock of 8 of the "Wooden
 * Hexagon Desk Model", which the suite's shop keeps for other tests.
 */
@DisplayName("Stock warnings in the cart")
class StockWarningsE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Short stock line warns")
  void aLineHoldingMoreThanTheStockLeftWarnsOfInsufficientStock() {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("stock_warnings_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ProductDetailPage product =
          ProductCatalogPage.navigateTo(page, shopUrl).viewProductNamed(DESK_MODEL);
      final String deskModel = product.shownPath();
      assertEquals(
          "8 units available",
          product.metaValue("Availability"),
          "The desk model starts with its seeded stock");
      addToCart(page, shopUrl, deskModel, 2);

      try (BrowserContext anotherGuest = browser.newContext(contextOptions())) {
        final Page other = anotherGuest.newPage();
        addToCart(other, shopUrl, deskModel, 7);
        placeTheOrder(other, shopUrl);
      }

      assertEquals(
          "Insufficient stock available",
          CartPage.navigateTo(page, shopUrl)
              .stockWarningOnceShown(DESK_MODEL, Duration.ofSeconds(15)));
    }
  }

  private static void placeTheOrder(final Page guest, final String shopUrl) {
    CartPage.navigateTo(guest, shopUrl)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery()
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .continueToPayment()
        .chooseTheOfferedProvider()
        .continueToReview()
        .pressPlaceOrder();
    assertThat(guest.locator("[data-test='confirmation-title']"))
        .hasText("Your order has been placed successfully");
  }

  private static void addToCart(
      final Page guest, final String shopUrl, final String productPath, final int times) {
    for (int i = 0; i < times; i++) {
      ProductDetailPage.navigateTo(guest, shopUrl, productPath).addToCart();
    }
  }
}
