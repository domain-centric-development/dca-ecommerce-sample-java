package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when the checkout would move to a step that is not reachable from where it stands.
 *
 * <p>The steps are an order, not a menu: a customer may return to an earlier one, but skipping
 * ahead would submit facts that depend on decisions not taken yet.
 */
public final class CheckoutStepOutOfOrderException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final CheckoutStep requested;
  private final CheckoutStep current;

  public CheckoutStepOutOfOrderException(
      final CheckoutSessionId sessionId, final CheckoutStep requested, final CheckoutStep current) {
    super("Cannot move to step " + requested + " from " + current);
    this.sessionId = sessionId;
    this.requested = requested;
    this.current = current;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  /** The step the caller asked for. */
  public CheckoutStep requested() {
    return requested;
  }

  /** The step the session stands at. */
  public CheckoutStep current() {
    return current;
  }
}
