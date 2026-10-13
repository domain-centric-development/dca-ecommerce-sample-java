package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest who reached the review sees the earlier steps done and the review as the current one. */
@DisplayName("The steps show the progress and are taken in order")
class StepsShowTheProgressE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Progress bar on the review")
  void theReviewMarksTheEarlierStepsAsDoneAndItselfAsCurrent() {
    aGuestWhoSubmittedBuyerDeliveryAndPayment();

    final Locator progress = page.locator("[data-test='checkout-progress']");
    assertThat(page).hasURL(BASE_URL + "/checkout/review");
    assertThat(progress.locator(".checkout__step--completed .checkout__step-label"))
        .hasText(new String[] {"Buyer Info", "Delivery", "Payment"});
    assertThat(progress.locator(".checkout__step--active .checkout__step-label"))
        .hasText(new String[] {"Review"});
  }

  private void aGuestWhoSubmittedBuyerDeliveryAndPayment() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    CartPage.navigateTo(page)
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
