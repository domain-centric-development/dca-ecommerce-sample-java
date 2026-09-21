package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a payment would be arranged for a checkout that owes nothing.
 *
 * <p>A payment intent for a total of zero would ask a provider to charge nothing, which every
 * provider answers differently and none of them usefully.
 */
public final class NothingToPayException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;

  public NothingToPayException(
      final CheckoutSessionId sessionId,
      final dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money total) {
    super("Nothing to pay: the total is " + total);
    this.sessionId = sessionId;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }
}
