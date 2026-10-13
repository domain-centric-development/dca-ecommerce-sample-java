package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.DeliveryPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest says on the delivery step where the order goes and how fast. */
@DisplayName("Choose the delivery address and the shipping method")
class ChooseTheDeliveryAddressE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Address and shipping open the payment step")
  void anAddressAndAShippingMethodOpenThePaymentStep() {
    final DeliveryPage delivery = aGuestOnTheDeliveryStep();

    delivery
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .pressContinue();

    assertThat(page).hasURL(BASE_URL + "/checkout/payment");
  }

  private DeliveryPage aGuestOnTheDeliveryStep() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    return CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery();
  }
}
