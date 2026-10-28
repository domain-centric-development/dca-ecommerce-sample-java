package dev.domaincentric.sample.ecommerce.checkout.application.orderconfirmation.sendorderconfirmation;

/**
 * Output model for sending the order confirmation.
 *
 * @param sessionId the checkout session the confirmation was about
 * @param outcome what became of the confirmation
 */
public record SendOrderConfirmationResult(String sessionId, Outcome outcome) {

  /** What became of the order confirmation. */
  public enum Outcome {
    /** The mail provider accepted the order confirmation. */
    SENT,
    /** No order was placed with that session; nothing can be sent, now or later. */
    NO_PLACED_ORDER
  }
}
