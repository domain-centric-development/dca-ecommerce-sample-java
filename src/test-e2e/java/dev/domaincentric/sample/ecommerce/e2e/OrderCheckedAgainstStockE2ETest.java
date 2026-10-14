package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest at the review presses "Place Order" after another visitor, in a browser of their own, has
 * bought the stock away: the order is refused and nothing changes. The scenario names the seeded
 * "Wooden Hexagon Desk Model"; the test orders a product of its own, created with the seeded stock
 * of 8, so no other test's order changes what is left.
 */
@DisplayName("Starting and placing check the cart against what can still be sold")
class OrderCheckedAgainstStockE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Place order is refused when stock ran short")
  void placingTheOrderAfterAnotherVisitorBoughtTheStockAwayIsRefused() throws Exception {
    final OwnProduct deskModel = OwnProduct.stocked("Stock Ran Short Desk Model", 8);
    assertEquals(
        "8 units available",
        productPage(page, deskModel).metaValue("Availability"),
        "The desk model starts with the stock it was created with");
    addToCart(page, deskModel, 5);
    toTheReview(page);

    try (BrowserContext anotherBrowser = browser.newContext(contextOptions())) {
      final Page anotherVisitor = anotherBrowser.newPage();
      addToCart(anotherVisitor, deskModel, 5);
      toTheReview(anotherVisitor).pressPlaceOrder();
      assertThat(anotherVisitor.locator("[data-test='confirmation-title']"))
          .hasText("Your order has been placed successfully");
    }
    assertEquals(
        "3 units available",
        productPage(page, deskModel)
            .metaValueOnceItReads("Availability", "3 units available", Duration.ofSeconds(15)),
        "The other visitor's order left 3");
    navigateTo("/checkout/review");
    final ReviewPage review = new ReviewPage(page);

    review.pressPlaceOrder();

    assertThat(page).hasURL(BASE_URL + "/checkout/review");
    assertThat(review.errorMessage()).isVisible();
    assertThat(page.locator("[data-test='confirmation-title']")).hasCount(0);
    assertEquals("3 units available", productPage(page, deskModel).metaValue("Availability"));
    assertEquals(List.of(deskModel.name() + " x5"), CartPage.navigateTo(page).lineItems());
  }

  private static ProductDetailPage productPage(final Page browserPage, final OwnProduct product) {
    browserPage.navigate(BASE_URL + product.path());
    return new ProductDetailPage(browserPage);
  }

  private static void addToCart(final Page browserPage, final OwnProduct product, final int times) {
    for (int i = 0; i < times; i++) {
      productPage(browserPage, product).addToCart();
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
