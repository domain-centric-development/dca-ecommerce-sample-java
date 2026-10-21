package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import org.jspecify.annotations.Nullable;

/**
 * Output model for resuming a checkout.
 *
 * @param resumed whether an open checkout of the customer's cart was found
 * @param sessionId the checkout session ID (null if none was found)
 */
public record ResumeCheckoutResult(boolean resumed, @Nullable String sessionId) {

  /** Creates a result saying the customer's cart has no open checkout. */
  public static ResumeCheckoutResult none() {
    return new ResumeCheckoutResult(false, null);
  }

  /** Builds the result from the session the customer resumes. */
  public static ResumeCheckoutResult from(final CheckoutSession session) {
    return new ResumeCheckoutResult(true, session.id().value());
  }
}
