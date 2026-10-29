package dev.domaincentric.sample.ecommerce.e2e;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ConfirmationPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The order confirmation mail a guest gets after placing the order, read from the mail provider the
 * suite's shop sends through. The suite's classes run in parallel against one shop and other tests
 * place orders for the same buyer, so the test reads only the mails of its own order — the key the
 * mail is sent under names the order reference the confirmation page shows.
 */
@DisplayName("The buyer gets an order confirmation")
class OrderConfirmationMailE2ETest extends BaseE2ETest {

  private static final String BUYER_EMAIL = "jane@example.com";

  private final ObjectMapper json = new ObjectMapper();
  private WireMockServer mailProvider;

  @BeforeEach
  void takeTheMailProvider() {
    mailProvider = ShopUnderTest.mailProvider().orElse(null);
    assumeTrue(
        mailProvider != null,
        "the shop was started elsewhere (e2e.baseUrl): its mail provider is not the suite's");
  }

  @Test
  @DisplayName("The buyer gets the order confirmation")
  void placingTheOrderSendsTheOrderConfirmationToTheBuyer() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    final ReviewPage review =
        CartPage.navigateTo(page).proceedToTheReviewWithBuyerEmail(BUYER_EMAIL);

    final ConfirmationPage confirmation = review.placeOrder();

    final String totalShown = confirmation.orderTotal().textContent().trim();
    final String key = "order-confirmation-" + confirmation.orderReference();
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .untilAsserted(() -> assertThat(mailsTo(BUYER_EMAIL, key)).isNotEmpty());
    final List<JsonNode> mails = mailsTo(BUYER_EMAIL, key);
    assertThat(mails)
        .as("the mail provider has received one mail to the buyer for this order")
        .singleElement()
        .satisfies(
            mail -> assertThat(mail.path("template").asText()).isEqualTo("order-confirmation"));
    assertThat(mails.getFirst().path("data").path("total").asText())
        .as("the mail carries the order total the confirmation page shows")
        .isEqualTo(totalShown);
  }

  /** The bodies of every mail request to that address under that key the provider received. */
  private List<JsonNode> mailsTo(final String address, final String key) {
    return mailProvider.findAll(postRequestedFor(urlEqualTo("/messages"))).stream()
        .map(request -> parse(request.getBodyAsString()))
        .filter(body -> address.equals(body.path("to").asText()))
        .filter(body -> key.equals(body.path("key").asText()))
        .toList();
  }

  private JsonNode parse(final String body) {
    try {
      return json.readTree(body);
    } catch (final Exception e) {
      throw new AssertionError("the mail request is not JSON: " + body, e);
    }
  }
}
