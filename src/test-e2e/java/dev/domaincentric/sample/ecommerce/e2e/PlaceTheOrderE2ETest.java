package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest places the order from the review step. The suite's shop pays through its provider stub,
 * so the guest chooses the one provider it offers.
 */
@DisplayName("Place the order")
class PlaceTheOrderE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Guest places the order")
  void placingTheOrderOpensTheConfirmation() {
    final ReviewPage review = aGuestOnTheReviewStep();

    review.pressPlaceOrder();

    assertThat(page).hasURL(BASE_URL + "/checkout/confirmation");
    assertThat(page.locator("[data-test='confirmation-title']"))
        .hasText("Your order has been placed successfully");
  }

  private ReviewPage aGuestOnTheReviewStep() {
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
        .continueToPayment()
        .chooseTheOfferedProvider()
        .continueToReview();
  }
}
