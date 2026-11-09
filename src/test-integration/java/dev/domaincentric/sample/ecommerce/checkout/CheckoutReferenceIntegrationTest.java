package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Every payment request names the checkout it pays for: Place Order through its input port, the
 * shop wired to a payment provider that is a stub on a free port, answering per checkout reference
 * what the test arranges. The requests are counted from the moment the checkouts stand at the
 * review.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
class CheckoutReferenceIntegrationTest {

  private static final String PROVIDER_ID = "provider";

  @RegisterExtension
  static WireMockExtension paymentProvider =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @DynamicPropertySource
  static void pointTheShopAtTheProvider(final DynamicPropertyRegistry registry) {
    registry.add("checkout.payment-provider.base-url", paymentProvider::baseUrl);
  }

  @Autowired private GetOrCreateActiveCartInputPort getOrCreateActiveCart;
  @Autowired private AddItemToCartInputPort addItemToCart;
  @Autowired private GetAllProductsInputPort getAllProducts;
  @Autowired private StartCheckoutInputPort startCheckout;
  @Autowired private SubmitBuyerInfoInputPort submitBuyerInfo;
  @Autowired private SubmitDeliveryInputPort submitDelivery;
  @Autowired private SubmitPaymentInputPort submitPayment;
  @Autowired private GetCheckoutReviewInputPort getCheckoutReview;
  @Autowired private ConfirmCheckoutInputPort confirmCheckout;
  @Autowired private GetCheckoutSessionInputPort getCheckoutSession;

  @Test
  @DisplayName("Two checkouts send two references")
  void twoCheckoutsSendTwoDifferentReferences() {
    providerAuthorizesEveryPayment();
    final CustomersCheckout first = checkoutAtTheReview();
    final CustomersCheckout second = checkoutAtTheReview();
    paymentProvider.resetRequests();

    assertInstanceOf(ConfirmCheckoutResult.Confirmed.class, placeOrder(first));
    assertInstanceOf(ConfirmCheckoutResult.Confirmed.class, placeOrder(second));

    final List<@Nullable String> references = merchantReferences();
    assertThat(references).hasSize(2).doesNotContainNull().allMatch(r -> !r.isBlank());
    assertThat(references.get(0)).isNotEqualTo(references.get(1));
  }

  @Test
  @DisplayName("The provider answers per checkout")
  void theProvidersAnswerForOneCheckoutTouchesNoOther() {
    providerAuthorizesEveryPayment();
    final CustomersCheckout first = checkoutAtTheReview();
    final CustomersCheckout second = checkoutAtTheReview();
    providerRefusesThePaymentOf(first.sessionId());

    assertThat(placeOrder(first)).isInstanceOf(ConfirmCheckoutResult.PaymentInitiationFailed.class);
    final ConfirmCheckoutResult.Confirmed placed =
        assertInstanceOf(ConfirmCheckoutResult.Confirmed.class, placeOrder(second));

    assertThat(currentStep(first)).isEqualTo(CheckoutStep.REVIEW);
    assertThat(placed.status()).isEqualTo(CheckoutSessionStatus.CONFIRMED.name());
  }

  @Test
  @DisplayName("A second attempt names the same checkout")
  void aSecondAttemptCarriesTheSameReference() {
    providerAuthorizesEveryPayment();
    final CustomersCheckout checkout = checkoutAtTheReview();
    paymentProvider.resetRequests();
    paymentProvider.resetMappings();
    paymentProvider.stubFor(post("/payments").willReturn(aResponse().withStatus(402)));
    assertThat(placeOrder(checkout))
        .isInstanceOf(ConfirmCheckoutResult.PaymentInitiationFailed.class);
    paymentProvider.resetMappings();
    providerAuthorizesEveryPayment();

    assertInstanceOf(ConfirmCheckoutResult.Confirmed.class, placeOrder(checkout));

    final List<@Nullable String> references = merchantReferences();
    assertThat(references).hasSize(2).containsOnly(checkout.sessionId());
  }

  private void providerAuthorizesEveryPayment() {
    paymentProvider.stubFor(
        post("/payments")
            .atPriority(10)
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"reference\": \"pay-" + UUID.randomUUID() + "\"}")));
  }

  private void providerRefusesThePaymentOf(final String sessionId) {
    paymentProvider.stubFor(
        post("/payments")
            .withRequestBody(matchingJsonPath("$.merchantReference", equalTo(sessionId)))
            .atPriority(1)
            .willReturn(aResponse().withStatus(402)));
  }

  /** The {@code merchantReference} of every payment request the provider received, in order. */
  private List<@Nullable String> merchantReferences() {
    return paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments"))).stream()
        .map(request -> Json.read(request.getBodyAsString(), Map.class).get("merchantReference"))
        .map(reference -> reference == null ? null : reference.toString())
        .toList();
  }

  /** Place Order from the review as it is shown now. */
  private ConfirmCheckoutResult placeOrder(final CustomersCheckout checkout) {
    final String reviewFingerprint =
        assertInstanceOf(
                GetCheckoutReviewResult.Found.class,
                getCheckoutReview.execute(
                    new GetCheckoutReviewQuery(checkout.sessionId(), checkout.customerId())))
            .fingerprint();
    return confirmCheckout.execute(
        new ConfirmCheckoutCommand(checkout.sessionId(), checkout.customerId(), reviewFingerprint));
  }

  private CheckoutStep currentStep(final CustomersCheckout checkout) {
    return assertInstanceOf(
            GetCheckoutSessionResult.Found.class,
            getCheckoutSession.execute(
                GetCheckoutSessionQuery.of(checkout.sessionId(), checkout.customerId())))
        .session()
        .step();
  }

  /**
   * A customer of their own whose checkout of one item has passed the buyer, delivery and payment
   * step and stands at the review.
   */
  private CustomersCheckout checkoutAtTheReview() {
    final String customerId = "payer-" + UUID.randomUUID();
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
    addItemToCart.execute(new AddItemToCartCommand(cartId, customerId, anyProductId(), 1));
    final String sessionId =
        assertInstanceOf(
                StartCheckoutResult.Started.class,
                startCheckout.execute(new StartCheckoutCommand(cartId, customerId)))
            .sessionId();

    assertInstanceOf(
        SubmitBuyerInfoResult.Submitted.class,
        submitBuyerInfo.execute(
            new SubmitBuyerInfoCommand(
                sessionId, customerId, "ada@example.com", "Ada", "Lovelace", "+1-555-0100")));
    assertInstanceOf(
        SubmitDeliveryResult.Submitted.class,
        submitDelivery.execute(
            new SubmitDeliveryCommand(
                sessionId,
                customerId,
                "123 Main Street",
                null,
                "Springfield",
                "12345",
                "United States",
                "IL",
                "standard")));
    assertInstanceOf(
        SubmitPaymentResult.Submitted.class,
        submitPayment.execute(new SubmitPaymentCommand(sessionId, customerId, PROVIDER_ID)));

    return new CustomersCheckout(sessionId, customerId);
  }

  private String anyProductId() {
    return getAllProducts
        .execute(new GetAllProductsQuery())
        .products()
        .getFirst()
        .productId()
        .value()
        .toString();
  }

  private record CustomersCheckout(String sessionId, String customerId) {}
}
