package dev.domaincentric.sample.ecommerce.checkout;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment.PaymentProviderProperties;
import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment.RestPaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider.PaymentResult;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider.PaymentResult.Outcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * The cancellation of an authorized payment, as the REST adapter puts it to the provider and reads
 * its answer: the provider is a stub on a free port.
 */
class PaymentCancellationIntegrationTest {

  private static final String CANCEL_URL = "/payments/pay-1/cancel";

  @RegisterExtension
  static WireMockExtension paymentProvider =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  private RestPaymentProvider adapter() {
    return new RestPaymentProvider(new PaymentProviderProperties(paymentProvider.baseUrl()));
  }

  @Test
  @DisplayName("204 cancels the payment, asked for without a body")
  void noContentCancelsThePayment() {
    paymentProvider.stubFor(post(CANCEL_URL).willReturn(aResponse().withStatus(204)));

    final PaymentResult result = adapter().cancelPayment("pay-1");

    assertThat(result.outcome()).isEqualTo(Outcome.SUCCEEDED);
    assertThat(paymentProvider.findAll(postRequestedFor(urlEqualTo(CANCEL_URL))))
        .singleElement()
        .satisfies(request -> assertThat(request.getBodyAsString()).isEmpty());
  }

  @Test
  @DisplayName("Another answer refuses the cancellation")
  void anotherAnswerRefusesTheCancellation() {
    paymentProvider.stubFor(post(CANCEL_URL).willReturn(aResponse().withStatus(409)));

    final PaymentResult result = adapter().cancelPayment("pay-1");

    assertThat(paymentProvider.findAll(postRequestedFor(urlEqualTo(CANCEL_URL)))).hasSize(1);
    assertThat(result.outcome()).isEqualTo(Outcome.REFUSED);
  }

  @Test
  @DisplayName("No answer within 2 seconds counts as unavailable")
  void noAnswerInTimeCountsAsUnavailable() {
    paymentProvider.stubFor(
        post(CANCEL_URL).willReturn(aResponse().withStatus(204).withFixedDelay(3000)));

    final PaymentResult result = adapter().cancelPayment("pay-1");

    assertThat(result.outcome()).isEqualTo(Outcome.UNAVAILABLE);
  }
}
