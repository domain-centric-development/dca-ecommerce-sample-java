package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart.GetOrCreateActiveCartInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getshippingoptions.GetShippingOptionsInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getshippingoptions.GetShippingOptionsQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getshippingoptions.GetShippingOptionsResult.ShippingOptionData;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Paying again for a checkout whose payment the provider already authorized: the payment use case
 * through its input port, the shop wired to a payment provider that is a stub on a free port.
 */
// The shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring context — so this class gets a
// database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:paying_again;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
class PayingAgainIntegrationTest {

  private static final String PROVIDER_ID = "provider";
  private static final String FIRST_PAYMENT = "pay-first";

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
  @Autowired private GetShippingOptionsInputPort getShippingOptions;
  @Autowired private SubmitDeliveryInputPort submitDelivery;
  @Autowired private SubmitPaymentInputPort submitPayment;

  @Test
  @DisplayName("Paying again for a changed amount replaces the payment")
  void payingAgainForAChangedAmountCancelsTheFirstPaymentAndRequestsTheNewTotal() {
    final String customerId = "payer-again";
    final String sessionId = checkoutWithDomainDrivenDesignOnce(customerId);
    chooseShipping(sessionId, customerId, "Standard Shipping");
    providerAuthorizesWith(FIRST_PAYMENT);
    pay(sessionId, customerId);
    assertThat(amountOf(paymentRequests().getFirst())).isEqualTo("59.98");
    providerAuthorizesWith("pay-second");
    paymentProvider.stubFor(
        post("/payments/" + FIRST_PAYMENT + "/cancel").willReturn(aResponse().withStatus(204)));
    chooseShipping(sessionId, customerId, "Express Shipping");

    final SubmitPaymentResult paid = pay(sessionId, customerId);

    assertThat(urlsReceived())
        .as("the provider received the cancellation of the first payment before a new request")
        .containsExactly("/payments", "/payments/" + FIRST_PAYMENT + "/cancel", "/payments");
    final List<LoggedRequest> requests = paymentRequests();
    assertThat(amountOf(requests.get(1))).as("the second payment request").isEqualTo("64.98");
    assertThat(merchantReferenceOf(requests.get(1)))
        .isEqualTo(merchantReferenceOf(requests.get(0)));
    assertThat(paid.currentStep()).isEqualTo(CheckoutStep.REVIEW.name());
  }

  private void providerAuthorizesWith(final String paymentReference) {
    paymentProvider.stubFor(
        post("/payments")
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"reference\": \"" + paymentReference + "\"}")));
  }

  /** Every URL the provider received, in the order it received them. */
  private List<String> urlsReceived() {
    return paymentProvider.findAll(anyRequestedFor(anyUrl())).stream()
        .map(LoggedRequest::getUrl)
        .toList();
  }

  private List<LoggedRequest> paymentRequests() {
    return paymentProvider.findAll(postRequestedFor(urlEqualTo("/payments")));
  }

  private static String amountOf(final LoggedRequest request) {
    return String.valueOf(Json.read(request.getBodyAsString(), Map.class).get("amount"));
  }

  private static String merchantReferenceOf(final LoggedRequest request) {
    return String.valueOf(Json.read(request.getBodyAsString(), Map.class).get("merchantReference"));
  }

  private SubmitPaymentResult pay(final String sessionId, final String customerId) {
    return submitPayment.execute(new SubmitPaymentCommand(sessionId, customerId, PROVIDER_ID));
  }

  private void chooseShipping(
      final String sessionId, final String customerId, final String shippingName) {
    final ShippingOptionData option =
        getShippingOptions.execute(GetShippingOptionsQuery.create()).shippingOptions().stream()
            .filter(o -> o.name().equals(shippingName))
            .findFirst()
            .orElseThrow();
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
            option.id()));
  }

  /** A checkout of "Domain-Driven Design" once whose buyer step is done. */
  private String checkoutWithDomainDrivenDesignOnce(final String customerId) {
    final String cartId =
        getOrCreateActiveCart.execute(new GetOrCreateActiveCartCommand(customerId)).cartId();
    addItemToCart.execute(
        new AddItemToCartCommand(cartId, customerId, productIdOf("Domain-Driven Design"), 1));
    final String sessionId =
        startCheckout.execute(new StartCheckoutCommand(cartId, customerId)).sessionId();
    submitBuyerInfo.execute(
        new SubmitBuyerInfoCommand(
            sessionId, customerId, "ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    return sessionId;
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
}
