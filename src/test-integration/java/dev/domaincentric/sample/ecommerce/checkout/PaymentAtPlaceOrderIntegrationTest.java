package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview.GetCheckoutReviewQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
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
 * The payment step chooses how to pay, and Place Order requests the payment: the use cases through
 * their input ports, the shop wired to a payment provider that is a stub on a free port.
 */
// The shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring context — so this class gets a
// database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:payment_at_place_order;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class PaymentAtPlaceOrderIntegrationTest {

  private static final String PROVIDER_ID = "provider";
  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

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
  @Autowired private ProvisionInitialStockInputPort provisionInitialStock;

  @Test
  @DisplayName("Authorized payment moves to review")
  void choosingHowToPayRequestsNoPayment() {
    providerAuthorizesEveryPayment();
    final CustomersCheckout checkout = checkoutAtThePaymentStep(anyProductId(), 1);

    final SubmitPaymentResult chosen = choosePayment(checkout);

    assertThat(chosen.currentStep()).isEqualTo(CheckoutStep.REVIEW.name());
    assertThat(paymentRequests()).as("the payment provider received no payment request").isEmpty();
  }

  @Test
  @DisplayName("The payment request names its checkout")
  void placeOrderRequestsThePaymentWithTheCheckoutReference() {
    providerAuthorizesEveryPayment();
    final CustomersCheckout checkout = checkoutAtThePaymentStep(anyProductId(), 1);
    choosePayment(checkout);
    final Money total = totalOf(checkout);
    paymentProvider.resetRequests();

    placeOrder(checkout);

    final List<LoggedRequest> requests = paymentRequests();
    assertThat(requests).as("one payment request").hasSize(1);
    final Map<?, ?> request = Json.read(requests.getFirst().getBodyAsString(), Map.class);
    assertThat(request.get("amount")).isEqualTo(total.amount().toPlainString());
    assertThat(request.get("merchantReference")).isEqualTo(checkout.sessionId());
  }

  @Test
  @DisplayName("Place order refused for stock requests no payment")
  void anOrderRefusedForStockRequestsNoPayment() {
    providerAuthorizesEveryPayment();
    final String deskModel = productIdOf(DESK_MODEL);
    provisionInitialStock.execute(new ProvisionInitialStockCommand(deskModel, 8));
    final CustomersCheckout guest = checkoutAtThePaymentStep(deskModel, 5);
    choosePayment(guest);
    final CustomersCheckout anotherVisitor = checkoutAtThePaymentStep(deskModel, 5);
    choosePayment(anotherVisitor);
    assertThat(placeOrder(anotherVisitor).status())
        .isEqualTo(CheckoutSessionStatus.CONFIRMED.name());

    final CheckoutValidationException refused =
        catchThrowableOfType(CheckoutValidationException.class, () -> placeOrder(guest));

    assertThat(refused).as("Place Order is refused").isNotNull();
    assertThat(refused.validation().shortOfStock().map(ValidationError::productName))
        .contains(DESK_MODEL);
    assertThat(currentStep(guest)).isEqualTo(CheckoutStep.REVIEW);
    assertThat(paymentRequestsOf(guest))
        .as("the payment provider received no payment request for the guest's checkout")
        .isEmpty();
  }

  private void providerAuthorizesEveryPayment() {
    paymentProvider.stubFor(
        post("/payments")
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"reference\": \"pay-" + UUID.randomUUID() + "\"}")));
  }

  private List<LoggedRequest> paymentRequests() {
    return paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments")));
  }

  private List<LoggedRequest> paymentRequestsOf(final CustomersCheckout checkout) {
    return paymentProvider.findAll(
        postRequestedFor(urlEqualTo("/payments"))
            .withRequestBody(
                matchingJsonPath("$.merchantReference", equalTo(checkout.sessionId()))));
  }

  private SubmitPaymentResult choosePayment(final CustomersCheckout checkout) {
    return submitPayment.execute(
        new SubmitPaymentCommand(checkout.sessionId(), checkout.customerId(), PROVIDER_ID));
  }

  /** Place Order from the review as it is shown now. */
  private ConfirmCheckoutResult placeOrder(final CustomersCheckout checkout) {
    final String reviewFingerprint =
        getCheckoutReview
            .execute(new GetCheckoutReviewQuery(checkout.sessionId(), checkout.customerId()))
            .fingerprint();
    return confirmCheckout.execute(
        new ConfirmCheckoutCommand(checkout.sessionId(), checkout.customerId(), reviewFingerprint));
  }

  private Money totalOf(final CustomersCheckout checkout) {
    return getCheckoutSession
        .execute(GetCheckoutSessionQuery.of(checkout.sessionId(), checkout.customerId()))
        .session()
        .totals()
        .total();
  }

  private CheckoutStep currentStep(final CustomersCheckout checkout) {
    return getCheckoutSession
        .execute(GetCheckoutSessionQuery.of(checkout.sessionId(), checkout.customerId()))
        .session()
        .step();
  }

  /**
   * A customer of their own whose checkout of the product, that often, has passed the buyer and
   * delivery step.
   */
  private CustomersCheckout checkoutAtThePaymentStep(final String productId, final int quantity) {
    final String customerId = "payer-" + UUID.randomUUID();
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
    addItemToCart.execute(new AddItemToCartCommand(cartId, customerId, productId, quantity));
    final String sessionId =
        startCheckout.execute(new StartCheckoutCommand(cartId, customerId)).sessionId();

    submitBuyerInfo.execute(
        new SubmitBuyerInfoCommand(
            sessionId, customerId, "ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
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
            "standard"));

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

  private String productIdOf(final String name) {
    return getAllProducts.execute(new GetAllProductsQuery()).products().stream()
        .filter(product -> product.name().equals(name))
        .findFirst()
        .orElseThrow()
        .productId()
        .value()
        .toString();
  }

  private record CustomersCheckout(String sessionId, String customerId) {}
}
