package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import org.jspecify.annotations.Nullable;

/**
 * What placing the order came to: the order placed, or the refusal — a case of the result, not an
 * exception. Every refusing case leaves the session as it was and holds no stock.
 */
public sealed interface ConfirmCheckoutResult
    permits ConfirmCheckoutResult.Confirmed,
        ConfirmCheckoutResult.CheckoutSessionNotFound,
        ConfirmCheckoutResult.CheckoutNotModifiable,
        ConfirmCheckoutResult.CheckoutStepNotCompleted,
        ConfirmCheckoutResult.CheckoutStepOutOfOrder,
        ConfirmCheckoutResult.CheckoutValidation,
        ConfirmCheckoutResult.PriceChangedSinceReview,
        ConfirmCheckoutResult.OrderChangedSinceReview,
        ConfirmCheckoutResult.PaymentProviderNotFound,
        ConfirmCheckoutResult.PaymentProviderUnavailable,
        ConfirmCheckoutResult.PaymentInitiationFailed {

  /**
   * The order was placed.
   *
   * <p>A command's result is small: the session, where the checkout stands now and what state it is
   * in. The page that follows asks the session query for everything it displays.
   *
   * @param sessionId the checkout session ID
   * @param currentStep the current step after the command
   * @param status the session status
   */
  record Confirmed(String sessionId, String currentStep, String status)
      implements ConfirmCheckoutResult {

    /**
     * Builds the case from the session as it stands after the command.
     *
     * @param session the confirmed checkout session
     * @return the case
     */
    public static Confirmed from(final CheckoutSession session) {
      return new Confirmed(
          session.id().value(), session.currentStep().name(), session.status().name());
    }
  }

  /**
   * The caller has no such checkout — none exists, or it is somebody else's (ADR-036).
   *
   * @param sessionId the session the caller named
   */
  record CheckoutSessionNotFound(CheckoutSessionId sessionId)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The session no longer takes changes — the aggregate's rule.
   *
   * @param sessionId the session
   * @param status the status that refuses the order
   */
  record CheckoutNotModifiable(CheckoutSessionId sessionId, CheckoutSessionStatus status)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * A step still misses its data — the aggregate's rule.
   *
   * @param sessionId the session
   * @param step the step still to be completed
   */
  record CheckoutStepNotCompleted(CheckoutSessionId sessionId, CheckoutStep step)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The session does not stand at the review — the aggregate's rule.
   *
   * @param sessionId the session
   * @param requested the step the order is placed from
   * @param current the step the session stands at
   */
  record CheckoutStepOutOfOrder(
      CheckoutSessionId sessionId, CheckoutStep requested, CheckoutStep current)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * A line no longer passes the checkout pricing's checks, or its stock could not be reserved.
   *
   * @param validation the verdict, naming each line that refused
   */
  record CheckoutValidation(CheckoutValidationResult validation)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * A line's price changed since the review the customer was shown — the aggregate's rule.
   *
   * @param sessionId the session
   * @param productName the name of the line's product whose price changed, as the line shows it
   */
  record PriceChangedSinceReview(CheckoutSessionId sessionId, String productName)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The review at today's prices is no longer the one the customer was shown, for a reason other
   * than a price — the aggregate's rule.
   *
   * @param sessionId the session
   */
  record OrderChangedSinceReview(CheckoutSessionId sessionId)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The chosen payment provider is no longer one the shop offers.
   *
   * @param providerId the provider the session names
   */
  record PaymentProviderNotFound(PaymentProviderId providerId)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The chosen payment provider cannot take a payment right now, or did not answer; trying again
   * later is the sensible answer.
   *
   * @param providerId the provider the session names
   */
  record PaymentProviderUnavailable(PaymentProviderId providerId)
      implements ConfirmCheckoutResult, Refusal {}

  /**
   * The payment provider refused the payment.
   *
   * @param providerId the provider that refused
   * @param providerReason the provider's own reason, for the log — the shop has no way to judge it
   */
  record PaymentInitiationFailed(PaymentProviderId providerId, @Nullable String providerReason)
      implements ConfirmCheckoutResult, Refusal {}
}
