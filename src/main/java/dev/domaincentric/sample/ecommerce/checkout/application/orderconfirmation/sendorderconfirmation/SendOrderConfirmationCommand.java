package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

/**
 * Input model for sending the order confirmation of a confirmed checkout.
 *
 * @param sessionId the checkout session whose order is confirmed to the buyer
 */
public record SendOrderConfirmationCommand(String sessionId) {

  /** Compact constructor with validation. */
  public SendOrderConfirmationCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
  }
}
