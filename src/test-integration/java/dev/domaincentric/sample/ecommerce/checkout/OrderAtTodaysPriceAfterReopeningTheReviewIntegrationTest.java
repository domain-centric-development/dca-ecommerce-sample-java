package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutChangedSinceReviewException;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.LineItemSnapshot;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.changeprice.ChangePriceInputPort;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * "Place Order" against the review it was sent from, through the input ports of the wired
 * application: a change since that review refuses it and leaves the checkout open at the review;
 * placed again from the review as it now is, the order takes today's price and the payment provider
 * — a stub on a free port — authorizes the total that review shows.
 */
// A database of its own: the changed price must not reach the catalogue other test classes read.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:order_at_todays_price_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@DisplayName("Place Order against the review it was sent from")
class OrderAtTodaysPriceAfterReopeningTheReviewIntegrationTest {

  private static final String PROVIDER_ID = "provider";

  @RegisterExtension
  static WireMockExtension paymentProvider =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void pointTheShopAtTheProvider(final DynamicPropertyRegistry registry) {
    registry.add("checkout.payment-provider.base-url", paymentProvider::baseUrl);
  }

  @Autowired private ProductRepository products;
  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private StartCheckoutInputPort startCheckout;
  @Autowired private SubmitBuyerInfoInputPort submitBuyerInfo;
  @Autowired private SubmitDeliveryInputPort submitDelivery;
  @Autowired private SubmitPaymentInputPort submitPayment;
  @Autowired private GetCheckoutReviewInputPort getCheckoutReview;
  @Autowired private ChangePriceInputPort changePrice;
  @Autowired private ConfirmCheckoutInputPort confirmCheckout;
  @Autowired private GetCheckoutSessionInputPort getCheckoutSession;

  @Test
  @DisplayName("Place order after the refusal orders what the review now shows")
  void placeOrderAfterTheRefusalOrdersWhatTheReviewNowShows() {
    final String guest = "guest-" + UUID.randomUUID();
    final String ddd = productIdOf("Domain-Driven Design");
    final String sessionId = aCheckoutAtTheReview(guest, ddd);
    final GetCheckoutReviewResult shown =
        getCheckoutReview.execute(new GetCheckoutReviewQuery(sessionId, guest));
    assertThat(itemsOf(shown.review()))
        .as("the review as shown")
        .containsExactly("Domain-Driven Design x1 54.99 EUR");
    changePrice.execute(new ChangePriceCommand(ddd, new BigDecimal("49.99")));
    assertThatThrownBy(
            () ->
                confirmCheckout.execute(
                    new ConfirmCheckoutCommand(sessionId, guest, shown.fingerprint())))
        .as("Place Order on the review shown before the change")
        .isInstanceOf(CheckoutChangedSinceReviewException.class);
    final GetCheckoutReviewResult now =
        getCheckoutReview.execute(new GetCheckoutReviewQuery(sessionId, guest));
    assertThat(itemsOf(now.review()))
        .as("the review after the refusal")
        .containsExactly("Domain-Driven Design x1 49.99 EUR");

    confirmCheckout.execute(new ConfirmCheckoutCommand(sessionId, guest, now.fingerprint()));

    final CheckoutCartSnapshot ordered =
        getCheckoutSession.execute(GetCheckoutSessionQuery.of(sessionId, guest)).session();
    assertThat(ordered.status().name()).isEqualTo("CONFIRMED");
    assertThat(itemsOf(ordered)).containsExactly("Domain-Driven Design x1 49.99 EUR");
    assertThat(paymentRequestsOf(sessionId))
        .as("the payment provider received one payment request")
        .singleElement()
        .extracting(request -> Json.read(request.getBodyAsString(), Map.class).get("amount"))
        .isEqualTo("54.98");
  }

  @Test
  @DisplayName("Place order after a change in another tab is refused")
  void placeOrderAfterAChangeInAnotherTabIsRefused() {
    final String guest = "guest-" + UUID.randomUUID();
    final String sessionId = aCheckoutAtTheReview(guest, productIdOf("Domain-Driven Design"));
    final GetCheckoutReviewResult firstTab =
        getCheckoutReview.execute(new GetCheckoutReviewQuery(sessionId, guest));
    submitDelivery.execute(
        new SubmitDeliveryCommand(
            sessionId,
            guest,
            "Hauptstrasse 1",
            null,
            "Berlin",
            "10115",
            "Germany",
            null,
            "express"));

    final Throwable refusal =
        catchThrowable(
            () ->
                confirmCheckout.execute(
                    new ConfirmCheckoutCommand(sessionId, guest, firstTab.fingerprint())));

    assertThat(refusal)
        .isInstanceOfSatisfying(
            CheckoutChangedSinceReviewException.class,
            changed ->
                assertThat(changed.productWithChangedPrice()).as("no price named").isEmpty());
    final CheckoutCartSnapshot open =
        getCheckoutSession.execute(GetCheckoutSessionQuery.of(sessionId, guest)).session();
    assertThat(open.status().name()).as("the checkout stays open").isEqualTo("ACTIVE");
    assertThat(open.step().name()).as("at the review").isEqualTo("REVIEW");
    assertThat(
            getCheckoutReview
                .execute(new GetCheckoutReviewQuery(sessionId, guest))
                .review()
                .shippingOption()
                .name())
        .isEqualTo("Express Shipping");
  }

  /**
   * A guest's checkout of "Domain-Driven Design" once, with the buyer, "Standard Shipping" and the
   * provider stub entered, standing at the review; the stub authorizes every payment.
   */
  private String aCheckoutAtTheReview(final String guest, final String productId) {
    paymentProvider.stubFor(
        post("/payments")
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"reference\": \"pay-" + UUID.randomUUID() + "\"}")));
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(guest)).cartId();
    addItemToCart.execute(new AddItemToCartCommand(cartId, guest, productId, 1));
    final String sessionId =
        startCheckout.execute(new StartCheckoutCommand(cartId, guest)).sessionId();
    submitBuyerInfo.execute(
        new SubmitBuyerInfoCommand(
            sessionId, guest, "jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    submitDelivery.execute(
        new SubmitDeliveryCommand(
            sessionId,
            guest,
            "Hauptstrasse 1",
            null,
            "Berlin",
            "10115",
            "Germany",
            null,
            "standard"));
    submitPayment.execute(new SubmitPaymentCommand(sessionId, guest, PROVIDER_ID));
    return sessionId;
  }

  /** The payment requests the provider received for this checkout. */
  private static List<LoggedRequest> paymentRequestsOf(final String sessionId) {
    return paymentProvider.findAll(
        postRequestedFor(urlEqualTo("/payments"))
            .withRequestBody(matchingJsonPath("$.merchantReference", equalTo(sessionId))));
  }

  /** Each line as the pages show it: "name xQuantity line total". */
  private static List<String> itemsOf(final CheckoutCartSnapshot session) {
    return session.lineItems().stream()
        .map(
            (LineItemSnapshot item) ->
                item.name()
                    + " x"
                    + item.quantity()
                    + " "
                    + item.lineTotal().amount().toPlainString()
                    + " "
                    + item.lineTotal().currency().getCurrencyCode())
        .toList();
  }

  private String productIdOf(final String name) {
    return products.findAll().stream()
        .filter(product -> name.equals(product.name().value()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no product named " + name))
        .id()
        .value()
        .toString();
  }
}
