package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
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

  @Test
  @DisplayName("The confirmation promises no email")
  void theConfirmationPromisesNeitherAnEmailNorOrderTracking() {
    final ReviewPage review = aGuestOnTheReviewStep();

    review.pressPlaceOrder();

    assertThat(page.locator("[data-test='confirmation-title']"))
        .hasText("Your order has been placed successfully");
    final Locator confirmation = page.locator(".confirmation");
    assertThat(confirmation).not().containsText("We've sent a confirmation email");
    assertThat(confirmation)
        .not()
        .containsText("You can track your order status using your order reference.");
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
