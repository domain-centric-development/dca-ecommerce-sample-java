package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.DeliveryPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest goes back from the delivery step to the buyer step and finds it filled in. */
@DisplayName("Go back to an earlier step and change it")
class GoBackToAnEarlierStepE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Back from delivery opens the buyer step")
  void backFromTheDeliveryStepOpensTheBuyerStepFilledIn() {
    final DeliveryPage delivery = aGuestOnTheDeliveryStep();

    final BuyerInfoPage buyer = delivery.goBack();

    assertThat(page).hasURL(BASE_URL + "/checkout/buyer");
    assertThat(buyer.emailInput()).hasValue("jane@example.com");
    assertThat(buyer.firstNameInput()).hasValue("Jane");
    assertThat(buyer.lastNameInput()).hasValue("Doe");
    assertThat(buyer.phoneInput()).hasValue("+49 30 1234567");
  }

  private DeliveryPage aGuestOnTheDeliveryStep() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    return CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery();
  }
}
