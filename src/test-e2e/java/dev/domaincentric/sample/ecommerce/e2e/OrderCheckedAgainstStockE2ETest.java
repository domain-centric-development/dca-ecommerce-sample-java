package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
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
 * A guest at the review presses "Place Order" after another visitor, in a browser of their own, has
 * bought the stock away: the order is refused and nothing changes. The test runs a shop of its own,
 * started with nothing but the seed: the order it arranges uses up the seeded stock of the "Wooden
 * Hexagon Desk Model", which the suite's shop keeps for other tests.
 */
@DisplayName("Starting and placing check the cart against what can still be sold")
class OrderCheckedAgainstStockE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Place order is refused when stock ran short")
  void placingTheOrderAfterAnotherVisitorBoughtTheStockAwayIsRefused() {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith("order_checked_against_stock_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ProductDetailPage product =
          ProductCatalogPage.navigateTo(page, shopUrl).viewProductNamed(DESK_MODEL);
      final String deskModel = product.shownPath();
      assertEquals(
          "8 units available",
          product.metaValue("Availability"),
          "The desk model starts with its seeded stock");
      addToCart(page, shopUrl, deskModel, 5);
      toTheReview(page, shopUrl);

      try (BrowserContext anotherBrowser = browser.newContext(contextOptions())) {
        final Page anotherVisitor = anotherBrowser.newPage();
        addToCart(anotherVisitor, shopUrl, deskModel, 5);
        toTheReview(anotherVisitor, shopUrl).pressPlaceOrder();
        assertThat(anotherVisitor.locator("[data-test='confirmation-title']"))
            .hasText("Your order has been placed successfully");
      }
      assertEquals(
          "3 units available",
          ProductDetailPage.navigateTo(page, shopUrl, deskModel)
              .metaValueOnceItReads("Availability", "3 units available", Duration.ofSeconds(15)),
          "The other visitor's order left 3");
      page.navigate(shopUrl + "/checkout/review");
      final ReviewPage review = new ReviewPage(page, shopUrl);

      review.pressPlaceOrder();

      assertThat(page).hasURL(shopUrl + "/checkout/review");
      assertThat(review.errorMessage()).isVisible();
      assertThat(page.locator("[data-test='confirmation-title']")).hasCount(0);
      assertEquals(
          "3 units available",
          ProductDetailPage.navigateTo(page, shopUrl, deskModel).metaValue("Availability"));
      assertEquals(List.of(DESK_MODEL + " x5"), CartPage.navigateTo(page, shopUrl).lineItems());
    }
  }

  private static void addToCart(
      final Page browserPage, final String shopUrl, final String productPath, final int times) {
    for (int i = 0; i < times; i++) {
      ProductDetailPage.navigateTo(browserPage, shopUrl, productPath).addToCart();
    }
  }

  private static ReviewPage toTheReview(final Page browserPage, final String shopUrl) {
    return CartPage.navigateTo(browserPage, shopUrl)
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
        .continueToReview();
  }
}
