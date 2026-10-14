package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A checkout belongs to the browser that started it: a second browser on the same shop finds none.
 */
@DisplayName("A visitor never reaches another visitor's checkout")
class AnotherVisitorsCheckoutE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Another visitor finds no checkout")
  void aStrangerOpeningTheBuyerStepFindsNoCheckoutOfTheOwner() {
    anOwnerOnTheDeliveryStep();

    try (BrowserContext strangersBrowser = browser.newContext(contextOptions())) {
      final Page stranger = strangersBrowser.newPage();

      stranger.navigate(BASE_URL + "/checkout/buyer");

      assertThat(stranger).hasURL(BASE_URL + "/cart");
      assertThat(new CartPage(stranger).errorMessage()).hasText("No active checkout session found");
      assertThat(stranger.locator("[data-test='order-summary-item']")).hasCount(0);
      assertThat(stranger.locator("input[value='jane@example.com']")).hasCount(0);
    }
  }

  /**
   * The owner: a guest who started the checkout from a cart holding "Domain-Driven Design" once and
   * submitted the buyer step with "jane@example.com".
   */
  private void anOwnerOnTheDeliveryStep() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery();
  }
}
