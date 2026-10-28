package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation.SendOrderConfirmationInputPort;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Duration;
import java.util.List;
import org.awaitility.Awaitility;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Sending the order confirmation through the mail provider's REST contract ({@code POST /messages}:
 * {@code 202} accepted, anything else or no answer within 2 seconds unavailable). The provider is a
 * stub on a free port; the shop's mail provider address points at it.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:order_confirmation;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("The buyer gets an order confirmation")
class OrderConfirmationIntegrationTest {

  private static final String BUYER_EMAIL = "jane@example.com";
  private static final String TEMPLATE = "order-confirmation";
  private static final Duration SHORTLY = Duration.ofSeconds(15);

  @RegisterExtension
  static WireMockExtension mailProvider =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void pointTheShopAtTheMailProvider(final DynamicPropertyRegistry registry) {
    registry.add("checkout.mail-provider.base-url", mailProvider::baseUrl);
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private SendOrderConfirmationInputPort sendOrderConfirmation;

  private final ObjectMapper json = new ObjectMapper();

  @BeforeEach
  void forgetEarlierMails() {
    mailProvider.resetAll();
  }

  @Test
  @DisplayName("One order is confirmed once")
  void repeatedDeliveryReachesTheProviderUnderOneKey() throws Exception {
    mailProvider.stubFor(post("/messages").willReturn(aResponse().withStatus(202)));
    final String orderReference = orderReferenceOn(placedOrderOf(BUYER_EMAIL));

    sendOrderConfirmation.execute(new SendOrderConfirmationCommand(orderReference));
    sendOrderConfirmation.execute(new SendOrderConfirmationCommand(orderReference));

    Awaitility.await()
        .atMost(SHORTLY)
        .untilAsserted(() -> assertThat(mailsTo(BUYER_EMAIL)).hasSizeGreaterThanOrEqualTo(2));
    final List<JsonNode> mails = mailsTo(BUYER_EMAIL);
    assertThat(mails)
        .as("every mail for the order is the order confirmation")
        .allSatisfy(mail -> assertThat(mail.path("template").asText()).isEqualTo(TEMPLATE));
    assertThat(mails.stream().map(mail -> mail.path("key").asText()).distinct().toList())
        .as("every attempt for the order carries one and the same key")
        .singleElement()
        .satisfies(key -> assertThat(key).isNotBlank());
  }

  @Test
  @DisplayName("The confirmation follows once the mail provider is back")
  void theConfirmationFollowsOnceTheMailProviderIsBack() throws Exception {
    mailProvider.stubFor(post("/messages").willReturn(aResponse().withStatus(503)));
    final Document confirmation = placedOrderOf(BUYER_EMAIL);
    assertThat(confirmation.selectFirst("[data-test=confirmation-title]").text())
        .as("the order is placed although the mail provider is unavailable")
        .isEqualTo("Your order has been placed successfully");
    Awaitility.await()
        .atMost(SHORTLY)
        .untilAsserted(() -> assertThat(mailsTo(BUYER_EMAIL)).isNotEmpty());

    mailProvider.stubFor(post("/messages").willReturn(aResponse().withStatus(202)));

    Awaitility.await()
        .atMost(SHORTLY)
        .untilAsserted(() -> assertThat(acceptedMailsTo(BUYER_EMAIL)).isNotEmpty());
    Awaitility.await()
        .during(Duration.ofSeconds(2))
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(
            () ->
                assertThat(acceptedMailsTo(BUYER_EMAIL))
                    .as("exactly one order confirmation reaches the mail provider")
                    .singleElement()
                    .satisfies(
                        mail -> assertThat(mail.path("template").asText()).isEqualTo(TEMPLATE)));
  }

  /** A guest with that buyer e-mail placed an order; answers the confirmation page. */
  private Document placedOrderOf(final String buyerEmail) throws Exception {
    final GuestCheckout guest = new GuestCheckout(mockMvc);
    guest.toTheReviewAs(buyerEmail);
    return guest.placeTheOrder();
  }

  /** The order reference the confirmation page shows — the checkout session's id. */
  private static String orderReferenceOn(final Document confirmation) {
    return confirmation.selectFirst("[data-test=order-reference]").text().trim();
  }

  /** The bodies of every mail request to that address the provider received. */
  private List<JsonNode> mailsTo(final String address) {
    return mailRequestsTo(address, false);
  }

  /** The bodies of the mail requests to that address the provider accepted with {@code 202}. */
  private List<JsonNode> acceptedMailsTo(final String address) {
    return mailRequestsTo(address, true);
  }

  private List<JsonNode> mailRequestsTo(final String address, final boolean acceptedOnly) {
    return mailProvider.getAllServeEvents().stream()
        .filter(event -> "/messages".equals(event.getRequest().getUrl()))
        .filter(event -> !acceptedOnly || event.getResponse().getStatus() == 202)
        .map(ServeEvent::getRequest)
        .map(request -> parse(request.getBodyAsString()))
        .filter(body -> address.equals(body.path("to").asText()))
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
