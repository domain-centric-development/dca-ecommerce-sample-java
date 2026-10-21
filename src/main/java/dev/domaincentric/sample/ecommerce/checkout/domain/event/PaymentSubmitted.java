package dev.domaincentric.sample.ecommerce.checkout.domain.event;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Event indicating that the payment provider authorized a checkout session's payment.
 *
 * <p>This event is raised when the customer places the order at the review and the chosen provider
 * authorizes the session's total; choosing how to pay on the payment step raises none.
 */
public record PaymentSubmitted(
    UUID eventId, CheckoutSessionId sessionId, String paymentProviderId, Instant occurredOn)
    implements DomainEvent {

  public static PaymentSubmitted now(
      final CheckoutSessionId sessionId, final PaymentSelection payment) {
    return new PaymentSubmitted(
        UUID.randomUUID(), sessionId, payment.providerId().value(), Instant.now());
  }
}
