package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;

/**
 * What reading the review came to: the review at today's prices, or no such checkout for the caller
 * — a case of the result, not an exception.
 */
public sealed interface GetCheckoutReviewResult
    permits GetCheckoutReviewResult.Found, GetCheckoutReviewResult.CheckoutSessionNotFound {

  /**
   * The review of a checkout at today's prices, and the fingerprint "Place Order" carries of it.
   *
   * @param review the checkout as the review shows it: its lines and totals at today's prices
   * @param fingerprint the fingerprint of everything the review shows
   */
  record Found(CheckoutCartSnapshot review, String fingerprint) implements GetCheckoutReviewResult {

    public Found {
      if (review == null) {
        throw new IllegalArgumentException("Review cannot be null");
      }
      if (fingerprint == null || fingerprint.isBlank()) {
        throw new IllegalArgumentException("Fingerprint cannot be null or blank");
      }
    }
  }

  /**
   * The caller has no such checkout — none exists, or it is somebody else's (ADR-036).
   *
   * @param sessionId the session the caller named
   */
  record CheckoutSessionNotFound(CheckoutSessionId sessionId)
      implements GetCheckoutReviewResult, Refusal {}
}
