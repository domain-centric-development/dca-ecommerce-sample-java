package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Two guests, each in a browser of their own, press "Place Order" at the same moment for more than
 * the stock holds together: only the order the stock still covers is placed. The test runs a shop
 * of its own, started with nothing but the seed: the orders use up the seeded stock of the "Wooden
 * Hexagon Desk Model", which the suite's shop keeps for other tests.
 */
@DisplayName("Two orders placed at once never sell more than the stock")
class TwoOrdersAtOnceE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";
  private static final String CONFIRMATION_TITLE = "[data-test='confirmation-title']";
  private static final String REVIEW_ERROR = "[data-test='review-error-message']";

  @Test
  @DisplayName("The order the stock no longer covers is refused")
  void ofTwoOrdersPlacedAtOnceOnlyTheOneTheStockCoversIsPlaced() {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("two_orders_at_once_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ProductDetailPage product =
          ProductCatalogPage.navigateTo(page, shopUrl).viewProductNamed(DESK_MODEL);
      final String deskModel = product.shownPath();
      assertEquals(
          "8 units available",
          product.metaValue("Availability"),
          "The desk model starts with its seeded stock");

      try (BrowserContext secondBrowser = browser.newContext(contextOptions())) {
        final Page secondGuest = secondBrowser.newPage();
        final List<Page> guests = List.of(page, secondGuest);
        final List<ReviewPage> reviews =
            guests.stream()
                .map(
                    guest -> {
                      addToCart(guest, shopUrl, deskModel, 5);
                      return CartPage.navigateTo(guest, shopUrl).proceedToTheReviewAsJaneDoe();
                    })
                .toList();

        reviews.forEach(ReviewPage::pressPlaceOrder);

        guests.forEach(guest -> outcomeOn(guest).waitFor());
        final List<Page> confirmed =
            guests.stream()
                .filter(guest -> guest.locator(CONFIRMATION_TITLE).count() == 1)
                .toList();
        assertEquals(1, confirmed.size(), "exactly one of the two guests sees the confirmation");
        assertThat(confirmed.getFirst().locator(CONFIRMATION_TITLE))
            .hasText("Your order has been placed successfully");
        final Page refused =
            guests.stream()
                .filter(guest -> guest != confirmed.getFirst())
                .findFirst()
                .orElseThrow();
        assertThat(refused).hasURL(shopUrl + "/checkout/review");
        assertThat(refused.locator(REVIEW_ERROR))
            .hasText("Not enough stock left for '" + DESK_MODEL + "'. Please adjust your order.");
      }

      assertEquals(
          "3 units available",
          ProductDetailPage.navigateTo(page, shopUrl, deskModel)
              .metaValueOnceItReads("Availability", "3 units available", Duration.ofSeconds(15)),
          "Only the placed order lowered the stock");
    }
  }

  /**
   * What the guest's page settles on after "Place Order": the confirmation or the review's error.
   */
  private static Locator outcomeOn(final Page guest) {
    return guest.locator(CONFIRMATION_TITLE + ", " + REVIEW_ERROR).first();
  }

  private static void addToCart(
      final Page guest, final String shopUrl, final String productPath, final int times) {
    for (int i = 0; i < times; i++) {
      ProductDetailPage.navigateTo(guest, shopUrl, productPath).addToCart();
    }
  }
}
