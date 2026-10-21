package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

/**
 * Input model for confirming a checkout session.
 *
 * @param sessionId the checkout session ID to confirm
 * @param customerId the customer the caller is acting as
 * @param reviewFingerprint the fingerprint of the review the customer placed the order from
 */
public record ConfirmCheckoutCommand(
    String sessionId, String customerId, String reviewFingerprint) {

  /** Compact constructor with validation. */
  public ConfirmCheckoutCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
    if (reviewFingerprint == null) {
      throw new IllegalArgumentException("Review fingerprint cannot be null");
    }
  }
}
