package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;

/**
 * What submitting the buyer's details came to: the details taken, or the refusal — a case of the
 * result, not an exception.
 */
public sealed interface SubmitBuyerInfoResult
    permits SubmitBuyerInfoResult.Submitted,
        SubmitBuyerInfoResult.CheckoutSessionNotFound,
        SubmitBuyerInfoResult.CheckoutNotModifiable,
        SubmitBuyerInfoResult.CheckoutStepOutOfOrder {

  /**
   * The session took the buyer's details.
   *
   * <p>A command's result is small: the session, where the checkout stands now and what state it is
   * in. The page that follows asks the session query for everything it displays.
   *
   * @param sessionId the checkout session ID
   * @param currentStep the current step after the command
   * @param status the session status
   */
  record Submitted(String sessionId, String currentStep, String status)
      implements SubmitBuyerInfoResult {

    /**
     * Builds the case from the session as it stands after the command.
     *
     * @param session the updated checkout session
     * @return the case
     */
    public static Submitted from(final CheckoutSession session) {
      return new Submitted(
          session.id().value(), session.currentStep().name(), session.status().name());
    }
  }

  /**
   * The caller has no such checkout — none exists, or it is somebody else's (ADR-036). Nothing was
   * changed.
   *
   * @param sessionId the session the caller named
   */
  record CheckoutSessionNotFound(CheckoutSessionId sessionId)
      implements SubmitBuyerInfoResult, Refusal {}

  /**
   * The session no longer takes changes — the aggregate's rule.
   *
   * @param sessionId the session
   * @param status the status that refuses the change
   */
  record CheckoutNotModifiable(CheckoutSessionId sessionId, CheckoutSessionStatus status)
      implements SubmitBuyerInfoResult, Refusal {}

  /**
   * The step lies ahead of the step the session stands at — the aggregate's rule.
   *
   * @param sessionId the session
   * @param requested the step the submission belongs to
   * @param current the step the session stands at
   */
  record CheckoutStepOutOfOrder(
      CheckoutSessionId sessionId, CheckoutStep requested, CheckoutStep current)
      implements SubmitBuyerInfoResult, Refusal {}
}
