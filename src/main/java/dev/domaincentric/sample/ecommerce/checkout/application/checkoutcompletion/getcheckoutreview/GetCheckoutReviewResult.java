package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;

/**
 * The review of a checkout at today's prices, and the fingerprint "Place Order" carries of it.
 *
 * @param review the checkout as the review shows it: its lines and totals at today's prices
 * @param fingerprint the fingerprint of everything the review shows
 */
public record GetCheckoutReviewResult(CheckoutCartSnapshot review, String fingerprint) {

  public GetCheckoutReviewResult {
    if (review == null) {
      throw new IllegalArgumentException("Review cannot be null");
    }
    if (fingerprint == null || fingerprint.isBlank()) {
      throw new IllegalArgumentException("Fingerprint cannot be null or blank");
    }
  }
}
