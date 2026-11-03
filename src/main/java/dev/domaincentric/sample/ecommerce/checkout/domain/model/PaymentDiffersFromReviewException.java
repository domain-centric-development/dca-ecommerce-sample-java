package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.io.Serial;

/**
 * Raised when a checkout's payment is not the one its review showed: authorized by a provider other
 * than the chosen one, or for an amount other than the review's total.
 *
 * <p>The order is placed with the provider and at the total the review showed, and the payment
 * covers exactly that; an order and a payment that disagree are never confirmed.
 */
public final class PaymentDiffersFromReviewException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;

  private PaymentDiffersFromReviewException(
      final CheckoutSessionId sessionId, final String message) {
    super(message);
    this.sessionId = sessionId;
  }

  /**
   * The payment was authorized by a provider other than the one the review showed.
   *
   * @param sessionId the checkout
   * @param authorizedBy the provider that authorized the payment
   * @param chosen the provider the review showed
   * @return the failure
   */
  public static PaymentDiffersFromReviewException anotherProvider(
      final CheckoutSessionId sessionId,
      final PaymentProviderId authorizedBy,
      final PaymentProviderId chosen) {
    return new PaymentDiffersFromReviewException(
        sessionId,
        "Checkout "
            + sessionId.value()
            + " was authorized by "
            + authorizedBy.value()
            + " but its review shows "
            + chosen.value());
  }

  /**
   * The payment was authorized for an amount other than the review's total.
   *
   * @param sessionId the checkout
   * @param authorized the amount the payment was authorized for
   * @param reviewed the total the review showed
   * @return the failure
   */
  public static PaymentDiffersFromReviewException anotherAmount(
      final CheckoutSessionId sessionId, final Money authorized, final Money reviewed) {
    return new PaymentDiffersFromReviewException(
        sessionId,
        "Checkout "
            + sessionId.value()
            + " was authorized for "
            + authorized
            + " but its review totals "
            + reviewed);
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }
}
