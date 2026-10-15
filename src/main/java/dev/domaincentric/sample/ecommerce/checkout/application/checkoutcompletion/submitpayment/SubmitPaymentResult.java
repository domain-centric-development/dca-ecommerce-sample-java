package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;

/**
 * Output model for payment submission.
 *
 * <p>A command's result is small: the session, where the checkout stands now, what state it is in
 * and whether the payment the provider had already authorized was kept instead of requested anew.
 * The page that follows asks the session query for everything it displays.
 *
 * @param sessionId the checkout session ID
 * @param currentStep the current step after the command
 * @param status the session status
 * @param paymentKept whether the already authorized payment covered the total and was kept
 */
public record SubmitPaymentResult(
    String sessionId, String currentStep, String status, boolean paymentKept) {

  /**
   * Builds the result for a payment the provider has just authorized.
   *
   * @param session the updated checkout session
   * @return the result
   */
  public static SubmitPaymentResult from(final CheckoutSession session) {
    return of(session, false);
  }

  /**
   * Builds the result for a payment kept from before, with no new request to the provider.
   *
   * @param session the updated checkout session
   * @return the result
   */
  public static SubmitPaymentResult kept(final CheckoutSession session) {
    return of(session, true);
  }

  private static SubmitPaymentResult of(final CheckoutSession session, final boolean paymentKept) {
    return new SubmitPaymentResult(
        session.id().value(), session.currentStep().name(), session.status().name(), paymentKept);
  }
}
