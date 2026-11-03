package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import org.jspecify.annotations.Nullable;

/**
 * Input model for confirming a checkout session.
 *
 * @param sessionId the checkout session ID to confirm
 * @param customerId the customer the caller is acting as
 * @param reviewFingerprint the fingerprint of the review the customer placed the order from, {@code
 *     null} when none was sent — the review fingerprint answers it
 */
public record ConfirmCheckoutCommand(
    String sessionId, String customerId, @Nullable String reviewFingerprint) {

  /** Compact constructor with validation. */
  public ConfirmCheckoutCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
