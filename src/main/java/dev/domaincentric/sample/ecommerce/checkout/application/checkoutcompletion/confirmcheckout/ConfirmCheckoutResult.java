package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;

/**
 * Output model for checkout confirmation.
 *
 * <p>A command's result is small: the session, where the checkout stands now and what state it is
 * in. The page that follows asks the session query for everything it displays.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after the command
 * @param status the session status
 */
public record ConfirmCheckoutResult(String sessionId, String currentStep, String status) {

  /**
   * Builds the result from the session as it stands after the command.
   *
   * @param session the updated checkout session
   * @return the result
   */
  public static ConfirmCheckoutResult from(final CheckoutSession session) {
    return new ConfirmCheckoutResult(
        session.id().value(), session.currentStep().name(), session.status().name());
  }
}
