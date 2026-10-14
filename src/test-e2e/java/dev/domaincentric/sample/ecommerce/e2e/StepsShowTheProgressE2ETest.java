package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest who reached the review sees the earlier steps done and the review as the current one. */
@DisplayName("The steps show the progress and are taken in order")
class StepsShowTheProgressE2ETest extends BaseE2ETest {

  // Playwright matches the pattern as a JavaScript regex, which knows no \Q…\E quoting
  private static final Pattern BUYER_STEP =
      Pattern.compile(
          (BASE_URL + "/checkout/buyer").replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0")
              + "(\\?.*)?$");

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

  @Test
  @DisplayName("Delivery sent before the buyer step")
  void aDeliveryFormSentBeforeTheBuyerStepSaysThatThePreviousStepsComeFirst() {
    aGuestWhoHasJustStartedTheCheckout();

    sendADeliveryFormFromTheBuyerStep(
        Map.of(
            "street", "Hauptstrasse 1",
            "city", "Berlin",
            "postalCode", "10115",
            "country", "Germany",
            "shippingOptionId", "standard",
            "shippingOptionName", "Standard Shipping",
            "estimatedDelivery", "5-7 business days",
            "shippingCost", "4.99",
            "currencyCode", "EUR"));

    assertThat(page).hasURL(BUYER_STEP);
    // Not the BuyerInfoPage object: it waits for the URL without a query
    assertThat(page.locator("[data-test='buyer-error-message']"))
        .hasText("Please complete the previous checkout steps first.");

    page.navigate(BASE_URL + "/checkout/review");
    assertThat(page).hasURL(BUYER_STEP);
    assertThat(page.locator("[data-test='buyer-form']")).isVisible();
  }

  private void aGuestWhoHasJustStartedTheCheckout() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    CartPage.navigateTo(page).proceedToCheckout();
  }

  /**
   * Sends the delivery step's fields to the shop from the buyer page, its form token along — a
   * request the page does not offer, as a second tab or a replayed form would send it — and waits
   * for the buyer page it is led to.
   */
  private void sendADeliveryFormFromTheBuyerStep(final Map<String, String> fields) {
    page.waitForResponse(
        response ->
            "GET".equals(response.request().method())
                && "/checkout/buyer".equals(URI.create(response.url()).getPath()),
        () ->
            page.evaluate(
                "fields => {"
                    + " const form = document.querySelector(\"[data-test='buyer-form']\");"
                    + " form.action = '/checkout/delivery';"
                    + " for (const [name, value] of Object.entries(fields)) {"
                    + "   const input = document.createElement('input');"
                    + "   input.type = 'hidden'; input.name = name; input.value = value;"
                    + "   form.append(input);"
                    + " }"
                    + " form.submit();"
                    + "}",
                fields));
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
