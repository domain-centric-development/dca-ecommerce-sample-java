package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.PaymentPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest chooses on the payment step how to pay. The suite's shop pays through its provider stub,
 * so the one provider it offers is that one rather than the mock the shop offers without a provider
 * address.
 */
@DisplayName("Choose how to pay")
class ChooseHowToPayE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Choosing the provider opens the review")
  void choosingTheOfferedProviderOpensTheReviewStep() {
    final PaymentPage payment = aGuestOnThePaymentStep();

    payment.chooseTheOfferedProvider().pressContinue();

    assertThat(page).hasURL(BASE_URL + "/checkout/review");
  }

  private PaymentPage aGuestOnThePaymentStep() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    return CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery()
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .continueToPayment();
  }
}
