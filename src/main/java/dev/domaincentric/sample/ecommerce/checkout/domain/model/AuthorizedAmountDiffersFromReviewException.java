package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.io.Serial;

/**
 * Raised when a checkout would be confirmed at a total other than the amount its payment was
 * authorized for.
 *
 * <p>The order is placed at the total the review showed, and the payment covers exactly that; an
 * order and a payment that disagree are never confirmed.
 */
public final class AuthorizedAmountDiffersFromReviewException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;

  public AuthorizedAmountDiffersFromReviewException(
      final CheckoutSessionId sessionId, final Money authorized, final Money reviewed) {
    super(
        "Checkout "
            + sessionId.value()
            + " was authorized for "
            + authorized
            + " but its review totals "
            + reviewed);
    this.sessionId = sessionId;
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }
}
