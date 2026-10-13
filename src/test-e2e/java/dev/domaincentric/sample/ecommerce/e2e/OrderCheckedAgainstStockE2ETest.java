package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest at the review presses "Place Order" after another visitor, in a browser of their own, has
 * bought the stock away: the order is refused and nothing changes.
 */
@DisplayName("Starting and placing check the cart against what can still be sold")
class OrderCheckedAgainstStockE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Place order is refused when stock ran short")
  void placingTheOrderAfterAnotherVisitorBoughtTheStockAwayIsRefused() {
    assertEquals(
        "8 units available",
        ProductCatalogPage.navigateTo(page).viewProductNamed(DESK_MODEL).metaValue("Availability"),
        "The desk model starts with its seeded stock");
    addToCart(page, DESK_MODEL, 5);
    toTheReview(page);

    try (BrowserContext anotherBrowser = browser.newContext(contextOptions())) {
      final Page anotherVisitor = anotherBrowser.newPage();
      addToCart(anotherVisitor, DESK_MODEL, 5);
      toTheReview(anotherVisitor).pressPlaceOrder();
      assertThat(anotherVisitor.locator("[data-test='confirmation-title']"))
          .hasText("Your order has been placed successfully");
    }
    assertEquals(
        "3 units available",
        ProductCatalogPage.navigateTo(page)
            .viewProductNamed(DESK_MODEL)
            .metaValueOnceItReads("Availability", "3 units available", Duration.ofSeconds(15)),
        "The other visitor's order left 3");
    navigateTo("/checkout/review");
    final ReviewPage review = new ReviewPage(page);

    review.pressPlaceOrder();

    assertThat(page).hasURL(BASE_URL + "/checkout/review");
    assertThat(review.errorMessage()).isVisible();
    assertThat(page.locator("[data-test='confirmation-title']")).hasCount(0);
    assertEquals(
        "3 units available",
        ProductCatalogPage.navigateTo(page).viewProductNamed(DESK_MODEL).metaValue("Availability"));
    assertEquals(List.of(DESK_MODEL + " x5"), CartPage.navigateTo(page).lineItems());
  }

  private static void addToCart(final Page browserPage, final String productName, final int times) {
    for (int i = 0; i < times; i++) {
      ProductCatalogPage.navigateTo(browserPage).viewProductNamed(productName).addToCart();
    }
  }

  private static ReviewPage toTheReview(final Page browserPage) {
    return CartPage.navigateTo(browserPage)
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
