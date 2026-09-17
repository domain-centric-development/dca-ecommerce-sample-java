package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

/**
 * Input model for submitting payment information during checkout.
 *
 * <p>The provider reference is not part of the input: it is what the payment provider returns when
 * the payment is initiated.
 *
 * @param sessionId the checkout session ID
 * @param customerId the customer the caller is acting as
 * @param providerId the selected payment provider ID
 */
public record SubmitPaymentCommand(String sessionId, String customerId, String providerId) {

  /** Compact constructor with validation. */
  public SubmitPaymentCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
    if (providerId == null || providerId.isBlank()) {
      throw new IllegalArgumentException("Provider ID cannot be null or blank");
    }
  }
}
