package dev.domaincentric.sample.ecommerce.checkout.application.submitpayment;

/**
 * Input model for submitting payment information during checkout.
 *
 * <p>The provider reference is not part of the input: it is what the payment provider returns when
 * the payment is initiated.
 *
 * @param sessionId the checkout session ID
 * @param providerId the selected payment provider ID
 */
public record SubmitPaymentCommand(String sessionId, String providerId) {

  /** Compact constructor with validation. */
  public SubmitPaymentCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (providerId == null || providerId.isBlank()) {
      throw new IllegalArgumentException("Provider ID cannot be null or blank");
    }
  }
}
