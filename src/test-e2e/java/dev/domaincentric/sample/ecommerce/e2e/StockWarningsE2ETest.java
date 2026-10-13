package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest's cart line holds more than another guest's order left in stock: the cart page tells the
 * first guest on that line, before they try to check out.
 */
@DisplayName("Stock warnings in the cart")
class StockWarningsE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Short stock line warns")
  void aLineHoldingMoreThanTheStockLeftWarnsOfInsufficientStock() {
    addToCart(page, DESK_MODEL, 2);

    try (BrowserContext anotherGuest = browser.newContext(contextOptions())) {
      final Page other = anotherGuest.newPage();
      addToCart(other, DESK_MODEL, 7);
      placeTheOrder(other);
    }

    assertEquals(
        "Insufficient stock available",
        CartPage.navigateTo(page).stockWarningOnceShown(DESK_MODEL, Duration.ofSeconds(15)));
  }

  private static void placeTheOrder(final Page guest) {
    CartPage.navigateTo(guest)
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

  private static void addToCart(final Page guest, final String productName, final int times) {
    for (int i = 0; i < times; i++) {
      ProductCatalogPage.navigateTo(guest).viewProductNamed(productName).addToCart();
    }
  }
}
