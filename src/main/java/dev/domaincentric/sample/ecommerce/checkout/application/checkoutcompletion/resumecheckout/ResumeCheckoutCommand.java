package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout;

/**
 * Input model for resuming the open checkout of the customer's cart.
 *
 * @param customerId the customer the caller is acting as
 */
public record ResumeCheckoutCommand(String customerId) {

  /** Compact constructor with validation. */
  public ResumeCheckoutCommand {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
