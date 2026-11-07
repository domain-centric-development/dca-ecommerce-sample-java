package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.jspecify.annotations.Nullable;

/**
 * What reading a checkout session came to: the session for display, or no such checkout for the
 * caller — a case of the result, not a flag.
 */
public sealed interface GetCheckoutSessionResult
    permits GetCheckoutSessionResult.Found, GetCheckoutSessionResult.CheckoutSessionNotFound {

  /**
   * The session, as a {@link CheckoutCartSnapshot} read model. When the query named a step, {@link
   * #stepAccess()} carries the domain's decision whether that step may be opened; the adapter only
   * maps it to a route.
   *
   * @param session the checkout cart snapshot
   * @param stepAccess the access decision for the requested step (null if no step was requested)
   */
  record Found(CheckoutCartSnapshot session, @Nullable StepAccess stepAccess)
      implements GetCheckoutSessionResult {

    /**
     * The current step as a string.
     *
     * @return the current step name
     */
    public String currentStep() {
      return session.step().name();
    }

    /**
     * The status as a string.
     *
     * @return the status name
     */
    public String status() {
      return session.status().name();
    }
  }

  /**
   * The caller has no such checkout — none exists, or it is somebody else's (ADR-036). A caller
   * that wanted to open a step has no checkout to continue.
   *
   * @param sessionId the session the caller named
   */
  record CheckoutSessionNotFound(CheckoutSessionId sessionId)
      implements GetCheckoutSessionResult, Refusal {}
}
