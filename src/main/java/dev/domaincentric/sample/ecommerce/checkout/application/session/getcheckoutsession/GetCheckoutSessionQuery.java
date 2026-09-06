package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import org.jspecify.annotations.Nullable;

/**
 * Query model for retrieving a checkout session by ID.
 *
 * <p>A caller that wants to open a wizard page names the step it is about to show; the result then
 * also carries the domain's decision whether that step may be opened.
 *
 * @param sessionId the checkout session ID to retrieve
 * @param requestedStep the step the caller wants to open, or null when only the data is needed
 */
public record GetCheckoutSessionQuery(
    CheckoutSessionId sessionId, @Nullable CheckoutStep requestedStep) {

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
    return new GetCheckoutSessionQuery(sessionId, null);
  }

  /**
   * Creates a new query for the given session ID string.
   *
   * @param sessionId the session ID string
   * @return a new GetCheckoutSessionQuery
   */
  public static GetCheckoutSessionQuery of(final String sessionId) {
    return new GetCheckoutSessionQuery(CheckoutSessionId.of(sessionId), null);
  }

  /**
   * Creates a query that also asks whether the given step may be opened.
   *
   * @param sessionId the session ID string
   * @param step the step the caller wants to open
   * @return a new GetCheckoutSessionQuery
   */
  public static GetCheckoutSessionQuery forStep(final String sessionId, final CheckoutStep step) {
    if (step == null) {
      throw new IllegalArgumentException("Requested step cannot be null");
    }
    return new GetCheckoutSessionQuery(CheckoutSessionId.of(sessionId), step);
  }
}
