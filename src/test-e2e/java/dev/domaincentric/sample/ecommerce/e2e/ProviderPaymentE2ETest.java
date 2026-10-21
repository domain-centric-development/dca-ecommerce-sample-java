package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.domaincentric.sample.ecommerce.e2e.pages.*;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

/**
 * Paying at checkout through the payment provider the shop is pointed at.
 *
 * <p>Runs alone: it counts the payment requests the provider stub received, and every other class's
 * checkout pays through the same stub.
 */
@Isolated
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
  @DisplayName(
      "Place Order has the payment provider authorize the total the review showed and opens the confirmation")
  void placeOrderAuthorizesTheReviewedTotal() throws Exception {
    paymentProvider.stubFor(post("/payments").willReturn(ShopUnderTest.authorized()));
    ReviewPage review =
        ProductCatalogPage.navigateTo(page)
            .viewProductNamed("Domain-Driven Design")
            .addToCart()
            .proceedToTheReviewAsJaneDoe();
    assertEquals("59.98 EUR", review.total(), "the review's total");
    paymentProvider.resetRequests();

    ConfirmationPage confirmation = review.placeOrder();

    assertThat(page).hasURL(Pattern.compile(".*/checkout/confirmation$"));
    assertThat(confirmation.orderTotal()).hasText("59.98 EUR");
    List<LoggedRequest> requests =
        paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments")));
    assertEquals(1, requests.size(), "the payment provider received one payment request");
    JsonNode request = new ObjectMapper().readTree(requests.getFirst().getBodyAsString());
    assertEquals("59.98", request.get("amount").asText(), "the payment request's amount");
  }
}
