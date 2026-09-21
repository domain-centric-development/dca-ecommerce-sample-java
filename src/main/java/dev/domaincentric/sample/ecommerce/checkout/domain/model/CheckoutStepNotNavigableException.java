package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when the checkout would be navigated to a step nobody navigates to.
 *
 * <p>The confirmation step is reached by confirming, never by asking for it: it exists to show what
 * happened, so arriving there without the act it reports would show a purchase that was never made.
 */
public final class CheckoutStepNotNavigableException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final CheckoutStep step;

  public CheckoutStepNotNavigableException(
      final CheckoutSessionId sessionId, final CheckoutStep step) {
    super("Cannot navigate directly to step " + step);
    this.sessionId = sessionId;
    this.step = step;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  public CheckoutStep step() {
    return step;
  }
}
