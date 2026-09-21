package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a checkout session that is no longer open would be changed.
 *
 * <p>A session stops taking changes once it is confirmed, completed, abandoned or expired — from
 * then on it is the record of a purchase, not a form.
 */
public final class CheckoutNotModifiableException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final CheckoutSessionStatus status;

  public CheckoutNotModifiableException(
      final CheckoutSessionId sessionId, final CheckoutSessionStatus status) {
    super("Cannot modify checkout " + sessionId.value() + " with status: " + status);
    this.sessionId = sessionId;
    this.status = status;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  /** The status that refuses the change. */
  public CheckoutSessionStatus status() {
    return status;
  }
}
