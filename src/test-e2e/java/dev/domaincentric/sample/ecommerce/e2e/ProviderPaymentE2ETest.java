package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.domaincentric.sample.ecommerce.e2e.pages.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Paying at checkout through the payment provider the shop is pointed at. */
@DisplayName("Provider Payment E2E Tests")
class ProviderPaymentE2ETest extends BaseE2ETest {

  private WireMockServer paymentProvider;

  @BeforeEach
  void takeThePaymentProvider() {
    paymentProvider = ShopUnderTest.paymentProvider().orElse(null);
    assumeTrue(
        paymentProvider != null,
        "the shop was started elsewhere (e2e.baseUrl): its payment provider is not the suite's");
  }

  @Test
  @DisplayName("A payment the provider authorizes moves the checkout on to the review step")
  void authorizedPaymentMovesTheCheckoutToReview() throws Exception {
    PaymentPage payment = checkoutAtThePaymentStep();
    paymentProvider.stubFor(post("/payments").willReturn(ShopUnderTest.authorized()));
    paymentProvider.resetRequests();

    ReviewPage review = payment.selectFirstPaymentProvider().continueToReview();

    assertTrue(review.showsEmail("guest@example.com"), "the checkout shows the review step");
    List<LoggedRequest> requests =
        paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments")));
    assertEquals(1, requests.size(), "the payment provider received one payment request");
    String[] total = review.total().split("\\s+");
    JsonNode request = new ObjectMapper().readTree(requests.getFirst().getBodyAsString());
    assertEquals(
        0,
        new BigDecimal(total[0]).compareTo(new BigDecimal(request.get("amount").asText())),
        "the payment request is for the checkout session's total " + review.total());
    assertEquals(total[1], request.get("currency").asText(), "the payment request's currency");
  }

  private PaymentPage checkoutAtThePaymentStep() {
    ProductCatalogPage.navigateTo(page).viewFirstProduct().addToCart();
    return CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100")
        .continueToDelivery()
        .fillAddress("123 Main Street", "Springfield", "12345", "United States", "IL")
        .selectFirstShippingOption()
        .continueToPayment();
  }
}
