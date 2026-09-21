package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a checkout would be completed although it was never confirmed.
 *
 * <p>Completion records that the order left the shop. Only a confirmed session has an order to
 * record, so any other status refuses.
 */
public final class CheckoutNotConfirmedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final CheckoutSessionStatus status;

  public CheckoutNotConfirmedException(
      final CheckoutSessionId sessionId, final CheckoutSessionStatus status) {
    super("Cannot complete checkout " + sessionId.value() + " with status: " + status);
    this.sessionId = sessionId;
    this.status = status;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  public CheckoutSessionStatus status() {
    return status;
  }
}
