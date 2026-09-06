package dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;

/**
 * Query model for retrieving a checkout session by ID.
 *
 * @param sessionId the checkout session ID to retrieve
 */
public record GetCheckoutSessionQuery(CheckoutSessionId sessionId) {

  public GetCheckoutSessionQuery {
    if (sessionId == null) {
      throw new IllegalArgumentException("Session ID cannot be null");
    }
  }

  /**
   * Creates a new query for the given session ID.
   *
   * @param sessionId the session ID to query
   * @return a new GetCheckoutSessionQuery
   */
  public static GetCheckoutSessionQuery of(final CheckoutSessionId sessionId) {
    return new GetCheckoutSessionQuery(sessionId);
  }

  /**
   * Creates a new query for the given session ID string.
   *
   * @param sessionId the session ID string
   * @return a new GetCheckoutSessionQuery
   */
  public static GetCheckoutSessionQuery of(final String sessionId) {
    return new GetCheckoutSessionQuery(CheckoutSessionId.of(sessionId));
  }
}
