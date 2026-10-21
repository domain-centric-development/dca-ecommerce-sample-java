package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A guest at the review presses "Place Order" after an operator changed the price through the staff
 * API: the order is not what the review showed, so it is refused and the review shows today's. The
 * shop is one of its own, so the changed price never reaches the catalogue the rest of the suite
 * reads, and so is its payment provider, a stub on a free port whose requests only this test sees.
 */
@DisplayName("Order at the reviewed price")
class OrderAtTheReviewedPriceE2ETest extends BaseE2ETest {

  private WireMockServer paymentProvider;

  @BeforeEach
  void startThePaymentProvider() {
    paymentProvider = new WireMockServer(wireMockConfig().dynamicPort());
    paymentProvider.start();
    paymentProvider.stubFor(post("/payments").willReturn(ShopUnderTest.authorized()));
  }

  @AfterEach
  void stopThePaymentProvider() {
    paymentProvider.stop();
  }

  @Test
  @DisplayName(
      "Place Order after a price change since the review is refused and the review shows today's price")
  void placeOrderAfterAPriceChangeSinceTheReviewIsRefusedAndShowsTodaysPrice() throws Exception {
    try (ConfigurableApplicationContext shop =
        ShopUnderTest.startedWith(
            "order_at_the_reviewed_price_e2e",
            "checkout.payment-provider.base-url=" + paymentProvider.baseUrl())) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ReviewPage review = aGuestOnTheReviewStep(shopUrl);
      assertEquals(
          List.of("Domain-Driven Design x1 54.99 EUR"), review.items(), "the review as shown");
      assertEquals(
          204,
          ShopUnderTest.changePrice(
              shopUrl,
              ShopUnderTest.staffTokenOf(shop),
              ShopUnderTest.productIdOf(shopUrl, "Domain-Driven Design"),
              "49.99"),
          "the staff API's answer to the new price");

      review.pressPlaceOrder();

      assertThat(review.errorMessage())
          .hasText(
              "The price of 'Domain-Driven Design' has changed. Please check your order and place"
                  + " it again.");
      assertThat(page).hasURL(shopUrl + "/checkout/review");
      assertEquals(List.of("Domain-Driven Design x1 49.99 EUR"), review.items(), "the items");
      assertEquals("54.98 EUR", review.total(), "the total");
      assertThat(page.locator("[data-test='confirmation-title']")).hasCount(0);
      assertEquals(
          List.of(),
          paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments"))),
          "the payment provider received no payment request");
    }
  }

  private ReviewPage aGuestOnTheReviewStep(final String shopUrl) {
    return ProductCatalogPage.navigateTo(page, shopUrl)
        .viewProductNamed("Domain-Driven Design")
        .addToCart()
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
