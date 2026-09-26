package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse;
import org.springframework.web.client.RestClientException;

/**
 * The payment provider behind its REST contract — the anti-corruption layer between checkout and
 * the Payment Service Provider.
 *
 * <p>A payment is {@code POST /payments} with the amount and the currency. {@code 201} with a
 * payment reference means authorized, {@code 402} means refused. No answer within two seconds, no
 * connection, or any other answer counts as unavailable. The provider's wire types stay in this
 * class; the port sees only {@link PaymentResult}.
 *
 * <p>The contract knows no confirmation and no cancellation, so both answer with a refusal and call
 * nothing.
 *
 * <p>Active only where a provider address is configured; otherwise {@link MockPaymentProvider}
 * stands in.
 */
@Component
@ConditionalOnExpression("!'${checkout.payment-provider.base-url:}'.isBlank()")
public class RestPaymentProvider implements PaymentProvider {

  public static final PaymentProviderId PROVIDER_ID = PaymentProviderId.of("provider");
  private static final String DISPLAY_NAME = "Payment provider";
  private static final Duration TIMEOUT = Duration.ofSeconds(2);
  private static final Logger LOG = LoggerFactory.getLogger(RestPaymentProvider.class);
  private static final String NOT_IN_CONTRACT = "The payment provider's contract has no %s";

  private final RestClient restClient;

  public RestPaymentProvider(final PaymentProviderProperties properties) {
    final JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(
            // HTTP/1.1: the contract needs nothing more, and an h2c upgrade a provider cancels
            // mid-answer would turn its refusal into an unreachable provider
            HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT)
                .build());
    requestFactory.setReadTimeout(TIMEOUT);
    this.restClient =
        RestClient.builder()
            .baseUrl(Objects.requireNonNull(properties.baseUrl()))
            .requestFactory(requestFactory)
            .build();
  }

  @Override
  public PaymentProviderId providerId() {
    return PROVIDER_ID;
  }

  @Override
  public String displayName() {
    return DISPLAY_NAME;
  }

  @Override
  public PaymentResult initiatePayment(final CheckoutSessionId sessionId, final Money amount) {
    final PaymentRequest request =
        new PaymentRequest(amount.amount().toPlainString(), amount.currency().getCurrencyCode());
    try {
      return restClient
          .post()
          .uri("/payments")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange((httpRequest, response) -> translate(response, sessionId));
    } catch (final RestClientException e) {
      LOG.warn("Payment request for checkout session {} failed", sessionId, e);
      return PaymentResult.unavailable("The payment provider could not be reached");
    }
  }

  private static PaymentResult translate(
      final ConvertibleClientHttpResponse response, final CheckoutSessionId sessionId)
      throws IOException {
    final HttpStatusCode status = response.getStatusCode();
    if (status.isSameCodeAs(HttpStatus.PAYMENT_REQUIRED)) {
      return PaymentResult.failure("The payment provider refused the payment");
    }
    if (!status.isSameCodeAs(HttpStatus.CREATED)) {
      LOG.warn("Payment provider answered {} for checkout session {}", status, sessionId);
      return PaymentResult.unavailable("The payment provider answered " + status);
    }
    final PaymentAuthorization authorization = response.bodyTo(PaymentAuthorization.class);
    if (authorization == null
        || authorization.reference() == null
        || authorization.reference().isBlank()) {
      return PaymentResult.unavailable(
          "The payment provider authorized without a payment reference");
    }
    return PaymentResult.success(authorization.reference());
  }

  @Override
  public PaymentResult confirmPayment(final String providerReference) {
    return PaymentResult.failure(NOT_IN_CONTRACT.formatted("confirmation"));
  }

  @Override
  public PaymentResult cancelPayment(final String providerReference) {
    return PaymentResult.failure(NOT_IN_CONTRACT.formatted("cancellation"));
  }

  /**
   * Always {@code true}: the contract has no availability query, so unavailability shows on the
   * payment request itself.
   */
  @Override
  public boolean isAvailable() {
    return true;
  }

  /** The provider's payment request. */
  record PaymentRequest(String amount, String currency) {}

  /** The provider's answer to an authorized payment. */
  record PaymentAuthorization(@Nullable String reference) {}
}
