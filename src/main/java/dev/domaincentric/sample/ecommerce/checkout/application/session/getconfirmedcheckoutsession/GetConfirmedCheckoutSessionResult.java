package dev.domaincentric.sample.ecommerce.checkout.application.getconfirmedcheckoutsession;

import org.jspecify.annotations.Nullable;

/**
 * Output model for getting a confirmed or completed checkout session.
 *
 * @param found whether a confirmed/completed session was found
 * @param sessionId the checkout session ID (null if not found)
 * @param customerId the customer ID (null if not found)
 */
public record GetConfirmedCheckoutSessionResult(
    boolean found, @Nullable String sessionId, @Nullable String customerId) {

  /** Creates a response indicating no confirmed/completed session was found. */
  public static GetConfirmedCheckoutSessionResult notFound() {
    return new GetConfirmedCheckoutSessionResult(false, null, null);
  }

  /** Creates a response with the found session. */
  public static GetConfirmedCheckoutSessionResult of(
      final String sessionId, final String customerId) {
    return new GetConfirmedCheckoutSessionResult(true, sessionId, customerId);
  }
}
