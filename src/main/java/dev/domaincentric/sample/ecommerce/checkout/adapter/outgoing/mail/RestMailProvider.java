package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.mail;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.OrderConfirmationMailer;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.RoundingMode;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * The mail provider behind its REST contract — the anti-corruption layer between checkout and the
 * service that sends the shop's mails.
 *
 * <p>A mail is {@code POST /messages} with the recipient, the template that names the mail, the
 * idempotency key and the template's values as text. {@code 202} accepts it; any other answer, no
 * connection or no answer within two seconds counts as unavailable. The provider's wire types stay
 * in this class; the port sees only {@link Delivery}.
 *
 * <p>Active only where a provider address is configured; otherwise {@link StandInMailProvider}
 * stands in.
 */
@Component
@ConditionalOnExpression("!'${checkout.mail-provider.base-url:}'.isBlank()")
public class RestMailProvider implements OrderConfirmationMailer {

  private static final String ORDER_CONFIRMATION_TEMPLATE = "order-confirmation";
  private static final Duration TIMEOUT = Duration.ofSeconds(2);
  private static final Logger LOG = LoggerFactory.getLogger(RestMailProvider.class);

  private final RestClient restClient;

  public RestMailProvider(final MailProviderProperties properties) {
    final JdkClientHttpRequestFactory requestFactory =
        new JdkClientHttpRequestFactory(
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
  public Delivery send(final OrderConfirmationMail mail) {
    final MessageRequest request =
        new MessageRequest(
            mail.recipient(),
            ORDER_CONFIRMATION_TEMPLATE,
            mail.key(),
            Map.of("total", asShown(mail.orderTotal())));
    try {
      return restClient
          .post()
          .uri("/messages")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .exchange((httpRequest, response) -> translate(response.getStatusCode(), mail.key()));
    } catch (final RestClientException e) {
      LOG.warn("Mail {} could not be handed to the mail provider", mail.key(), e);
      return Delivery.UNAVAILABLE;
    }
  }

  /** {@code 202} accepts the mail; every other answer counts as unavailable. */
  private static Delivery translate(final HttpStatusCode status, final String key) {
    if (status.isSameCodeAs(HttpStatus.ACCEPTED)) {
      return Delivery.ACCEPTED;
    }
    LOG.warn("Mail provider answered {} to mail {}", status, key);
    return Delivery.UNAVAILABLE;
  }

  /** The amount as the shop's pages show it: two decimal places and the currency code. */
  private static String asShown(final Money amount) {
    return amount.amount().setScale(2, RoundingMode.HALF_UP).toPlainString()
        + " "
        + amount.currency().getCurrencyCode();
  }

  /** The provider's message request; {@code data} carries the template's values as text. */
  record MessageRequest(String to, String template, String key, Map<String, String> data) {}
}
