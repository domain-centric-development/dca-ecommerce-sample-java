package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a step is missing the data a later step depends on.
 *
 * <p>Each step of the checkout contributes facts the next ones need — where to deliver, who buys,
 * how it is paid. A step that was skipped leaves those facts absent, and the session says which
 * one.
 */
public final class CheckoutStepNotCompletedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final CheckoutStep step;

  public CheckoutStepNotCompletedException(
      final CheckoutSessionId sessionId, final CheckoutStep step) {
    super("Step " + step + " must be completed first");
    this.sessionId = sessionId;
    this.step = step;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  /** The step whose data is missing. */
  public CheckoutStep step() {
    return step;
  }
}
