package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockInputPort;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.awaitility.Awaitility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Placing the order on the review page through the payment provider's REST contract ({@code POST
 * /payments}: {@code 201} authorized, {@code 402} refused, no answer within 2 seconds unavailable).
 * The provider is a stub on a free port; the shop's provider address points at it.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:provider_payment;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
class ProviderPaymentIntegrationTest {

  private static final String REFUSED =
      "The payment was refused. Please choose another way to pay.";
  private static final String UNAVAILABLE =
      "The payment provider is not available right now. Please try again later.";
  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";
  private static final String REVIEW_STEP = "/checkout/review";

  @RegisterExtension
  static WireMockExtension paymentProvider =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void pointTheShopAtTheProvider(final DynamicPropertyRegistry registry) {
    registry.add("checkout.payment-provider.base-url", paymentProvider::baseUrl);
  }

  @Autowired private MockMvc mockMvc;
  @Autowired private ProvisionInitialStockInputPort provisionInitialStock;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("A payment the provider refuses at Place Order keeps the customer at the review")
  void refusedPaymentKeepsTheCustomerAtTheReview() throws Exception {
    checkoutAtTheReview(firstProductId(), 1);
    providerAnswers(aResponse().withStatus(402));

    final MvcResult placed = placeOrder();

    assertStaysAtTheReview(placed);
    assertThat(errorShownAfter(placed)).isEqualTo(REFUSED);
    assertNoConfirmation();
  }

  @Test
  @DisplayName("A refused payment holds no stock")
  void aRefusedPaymentGivesTheReservedStockBack() throws Exception {
    final String deskModel = productPageOf(DESK_MODEL).substring("/products/".length());
    provisionInitialStock.execute(new ProvisionInitialStockCommand(deskModel, 8));
    waitForAvailability(DESK_MODEL, "8 units available");
    checkoutAtTheReview(deskModel, 2);
    providerAnswers(aResponse().withStatus(402));

    final MvcResult placed = placeOrder();

    assertStaysAtTheReview(placed);
    assertThat(errorShownAfter(placed)).isEqualTo(REFUSED);
    assertThat(availabilityOf(DESK_MODEL)).isEqualTo("8 units available");
  }

  @Test
  @DisplayName("A provider that answers Place Order only after 5 seconds counts as unavailable")
  void slowProviderCountsAsUnavailable() throws Exception {
    checkoutAtTheReview(firstProductId(), 1);
    providerAnswers(
        aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"reference\": \"pay-slow\"}")
            .withFixedDelay(5_000));

    final MvcResult placed = placeOrder();

    assertStaysAtTheReview(placed);
    assertThat(errorShownAfter(placed)).isEqualTo(UNAVAILABLE);
    assertNoConfirmation();
  }

  @Test
  @DisplayName("A provider answering with a server error counts as unavailable")
  void providerServerErrorCountsAsUnavailable() throws Exception {
    checkoutAtTheReview(firstProductId(), 1);
    providerAnswers(aResponse().withStatus(500));

    final MvcResult placed = placeOrder();

    assertStaysAtTheReview(placed);
    assertThat(errorShownAfter(placed)).isEqualTo(UNAVAILABLE);
  }

  @Test
  @DisplayName("A provider that drops the connection counts as unavailable")
  void droppedConnectionCountsAsUnavailable() throws Exception {
    checkoutAtTheReview(firstProductId(), 1);
    providerAnswers(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER));

    final MvcResult placed = placeOrder();

    assertStaysAtTheReview(placed);
    assertThat(errorShownAfter(placed)).isEqualTo(UNAVAILABLE);
  }

  private void providerAnswers(final ResponseDefinitionBuilder answer) {
    paymentProvider.stubFor(post("/payments").willReturn(answer));
  }

  /** Presses "Place Order" on the review page. */
  private MvcResult placeOrder() throws Exception {
    return submit("/checkout/confirm", page(REVIEW_STEP), "sentFrom", "review");
  }

  /** The form post went back to the review page. */
  private static void assertStaysAtTheReview(final MvcResult placed) {
    assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);
  }

  /** No confirmed order to show: the confirmation page sends the visitor away. */
  private void assertNoConfirmation() throws Exception {
    assertThat(perform(get("/checkout/confirmation")).getResponse().getRedirectedUrl())
        .as("no confirmation is shown")
        .isEqualTo("/cart");
  }

  /** The review page the redirect leads to, with the message the redirect carried. */
  private String errorShownAfter(final MvcResult placed) throws Exception {
    final MvcResult page = perform(get(URI.create(placed.getResponse().getRedirectedUrl())));
    final Element error =
        Jsoup.parse(page.getResponse().getContentAsString())
            .selectFirst("[data-test=review-error-message]");
    assertThat(error).as("the review shows an error").isNotNull();
    return error.text();
  }

  /**
   * A guest's checkout at the review: the product in the cart that often, checkout started, buyer
   * and delivery filled in and the first payment provider chosen — all through the pages, so the
   * session is the visitor's own. The provider authorizes until the test arranges otherwise.
   */
  private void checkoutAtTheReview(final String productId, final int quantity) throws Exception {
    providerAnswers(
        aResponse()
            .withStatus(201)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"reference\": \"pay-1\"}"));
    for (int i = 0; i < quantity; i++) {
      submit(
          "/cart/add-product",
          page("/products/" + productId),
          "productId",
          productId,
          "quantity",
          "1");
    }

    final Document cart = page("/cart");
    submit("/checkout/start", cart, "cartId", cart.selectFirst("input[name=cartId]").val());

    submit(
        "/checkout/buyer",
        page("/checkout/buyer"),
        "email",
        "guest@example.com",
        "firstName",
        "Test",
        "lastName",
        "Guest",
        "phone",
        "+1-555-0100");

    final Document delivery = page("/checkout/delivery");
    final Element option =
        delivery.selectFirst("[data-test=delivery-shipping-radio]").closest(".option-card");
    submit(
        "/checkout/delivery",
        delivery,
        "street",
        "123 Main Street",
        "city",
        "Springfield",
        "postalCode",
        "12345",
        "country",
        "United States",
        "state",
        "IL",
        "shippingOptionId",
        option.selectFirst("input[name=shippingOptionId]").val(),
        "shippingOptionName",
        option.selectFirst("input[name=shippingOptionName]").val(),
        "estimatedDelivery",
        option.selectFirst("input[name=estimatedDelivery]").val(),
        "shippingCost",
        option.selectFirst("input[name=shippingCost]").val(),
        "currencyCode",
        option.selectFirst("input[name=currencyCode]").val());

    final Document payment = page("/checkout/payment");
    final MvcResult chosen =
        submit(
            "/checkout/payment",
            payment,
            "providerId",
            payment.selectFirst("[data-test=payment-provider-radio]").val());
    assertThat(chosen.getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);
  }

  private String firstProductId() throws Exception {
    return page("/products").selectFirst("a[href^=/products/]").attr("href").substring(10);
  }

  /**
   * Waits until the product page reads that availability — the stock changes shortly after an order
   * or a stock change elsewhere.
   */
  private void waitForAvailability(final String productName, final String availability)
      throws Exception {
    final String productPage = productPageOf(productName);
    Awaitility.await()
        .atMost(Duration.ofSeconds(15))
        .until(() -> availability.equals(availabilityOn(page(productPage))));
  }

  /** What the product page of the product with that name reads under "Availability". */
  private String availabilityOf(final String productName) throws Exception {
    return availabilityOn(page(productPageOf(productName)));
  }

  private String productPageOf(final String productName) throws Exception {
    return page("/products").select("[data-test=product-card]").stream()
        .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no card titled " + productName))
        .selectFirst("a[data-test=view-product]")
        .attr("href");
  }

  private static String availabilityOn(final Document productPage) {
    return productPage.select(".product-detail__meta-item").stream()
        .filter(
            item -> "Availability".equals(item.selectFirst(".product-detail__meta-label").text()))
        .findFirst()
        .map(item -> item.selectFirst(".product-detail__meta-value").text())
        .orElse(null);
  }

  private Document page(final String path) throws Exception {
    final MvcResult result = perform(get(path));
    assertThat(result.getResponse().getStatus()).as("GET " + path).isEqualTo(200);
    return Jsoup.parse(result.getResponse().getContentAsString());
  }

  /**
   * Posts a form the way the browser does: with the page's CSRF field and the visitor's cookies.
   */
  private MvcResult submit(final String path, final Document formPage, final String... fields)
      throws Exception {
    final MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.post(path)
            .param("_csrf", formPage.selectFirst("input[name=_csrf]").val());
    for (int i = 0; i < fields.length; i += 2) {
      request.param(fields[i], fields[i + 1]);
    }
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + path).isEqualTo(302);
    return result;
  }

  private MvcResult perform(final MockHttpServletRequestBuilder request) throws Exception {
    if (!cookies.isEmpty()) {
      request.cookie(cookies.values().toArray(Cookie[]::new));
    }
    final MvcResult result = mockMvc.perform(request).andReturn();
    for (final Cookie cookie : result.getResponse().getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
    return result;
  }
}
