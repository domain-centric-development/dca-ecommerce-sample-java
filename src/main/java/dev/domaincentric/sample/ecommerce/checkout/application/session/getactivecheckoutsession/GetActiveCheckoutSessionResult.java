package dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession;

import org.jspecify.annotations.Nullable;

/**
 * Output model for getting an active checkout session.
 *
 * @param found whether an active session was found
 * @param sessionId the checkout session ID (null if not found)
 * @param customerId the customer ID (null if not found)
 */
public record GetActiveCheckoutSessionResult(
    boolean found, @Nullable String sessionId, @Nullable String customerId) {

  /** Creates a response indicating no active session was found. */
  public static GetActiveCheckoutSessionResult notFound() {
    return new GetActiveCheckoutSessionResult(false, null, null);
  }

  /** Creates a response with the found session. */
  public static GetActiveCheckoutSessionResult of(final String sessionId, final String customerId) {
    return new GetActiveCheckoutSessionResult(true, sessionId, customerId);
  }
}
