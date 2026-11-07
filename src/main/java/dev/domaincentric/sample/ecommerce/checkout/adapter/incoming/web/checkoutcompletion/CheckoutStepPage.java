package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutRoutes;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * What every checkout step page does the same way: it opens only when the domain grants the step,
 * and it turns a refused submission into the redirect with its notice.
 *
 * <p>Each step controller switches over its use case's result and answers each refusing case with
 * the method here that names it — what a refusal looks like to the customer is decided here, once
 * per kind of refusal, and for a "Place Order" not sent from the review in {@link
 * #askForTheReview}. What is still thrown — a malformed value, a rule of the model no use case
 * returns — is caught by {@link #submitting}.
 */
final class CheckoutStepPage {

  private CheckoutStepPage() {}

  /** A step's submission: the use-case call and the redirect its result leads to. */
  @FunctionalInterface
  interface Submission {
    /**
     * Submits the step.
     *
     * @return the redirect the submission's result leads to
     */
    String submit();
  }

  /**
   * The redirect for a step the domain did not grant, or nothing for a granted one — which then
   * shows the notice a redirect handed it, as its error.
   *
   * <p>A denied step passes no notice on: a notice belongs to the page it was addressed to, and the
   * step shown instead says nothing it was not sent.
   *
   * @param access the domain's decision for the step
   * @param model the page's model, holding the notice a redirect handed the page, if any
   * @return the redirect to the step or the cart shown instead, or empty when the step opens
   */
  static Optional<String> redirectIfDenied(final StepAccess access, final Model model) {
    if (!access.granted()) {
      return Optional.of(CheckoutRoutes.redirectFor(access));
    }
    CheckoutStepNotice.showIn(model);
    return Optional.empty();
  }

  /**
   * The domain's decision whether a step may be opened, read from the session query asked for that
   * step: a checkout the caller does not have sends them back to the cart.
   *
   * @param result the session query's answer for the step
   * @return the decision
   */
  static StepAccess stepAccessOf(final GetCheckoutSessionResult result) {
    return switch (result) {
      case GetCheckoutSessionResult.Found found -> Objects.requireNonNull(found.stepAccess());
      case GetCheckoutSessionResult.CheckoutSessionNotFound notFound -> StepAccess.backToCart();
    };
  }

  /**
   * Submits a step: the submission's own switch answers every case of its result; what is still
   * thrown becomes the redirect back to the step with the notice for a malformed or unexpected
   * submission.
   *
   * @param step the step the submission belongs to
   * @param attributes the redirect's attributes, which take the notice
   * @param submission the use-case call and the answer to its result
   * @return the redirect the submission leads to
   */
  static String submitting(
      final CheckoutStep step, final RedirectAttributes attributes, final Submission submission) {
    try {
      return submission.submit();
    } catch (DomainException | IllegalArgumentException residue) {
      // IllegalArgumentException is still here because a form field the customer typed reaches a
      // value object unvalidated; each field moved into request validation is one reason less to
      // catch it. DomainException is a rule of the model no use case returns as a case.
      return CheckoutRoutes.redirectTo(step, residueNoticeFor(step, residue), attributes);
    }
  }

  /** The caller has no such checkout. */
  static String sessionNotFound(final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(step, CheckoutStepNotice.SESSION_NOT_FOUND, attributes);
  }

  /** The checkout no longer takes changes. */
  static String notModifiable(final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(step, CheckoutStepNotice.CHECKOUT_NOT_MODIFIABLE, attributes);
  }

  /**
   * A step sent while an earlier one still misses its data: to the step the domain sends the
   * customer to, saying so.
   */
  static String stepNotCompleted(
      final CheckoutStep step,
      final Supplier<StepAccess> access,
      final RedirectAttributes attributes) {
    return sentTooEarly(step, CheckoutStepNotice.EARLIER_STEPS_FIRST, access, attributes);
  }

  /**
   * A step sent ahead of the step the session stands at: an order placed out of order asks for the
   * review, any other step for the earlier steps.
   */
  static String stepOutOfOrder(
      final CheckoutStep step,
      final Supplier<StepAccess> access,
      final RedirectAttributes attributes) {
    final CheckoutStepNotice notice =
        step == CheckoutStep.REVIEW
            ? CheckoutStepNotice.REVIEW_FIRST
            : CheckoutStepNotice.EARLIER_STEPS_FIRST;
    return sentTooEarly(step, notice, access, attributes);
  }

  /** The shipping option chosen is not one the shop offers. */
  static String shippingMethodNotOffered(
      final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.SHIPPING_METHOD_NOT_OFFERED, attributes);
  }

  /** The way to pay chosen is not one the shop offers. */
  static String paymentMethodNotOffered(
      final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.PAYMENT_METHOD_NOT_OFFERED, attributes);
  }

  /** The payment provider cannot take a payment right now. */
  static String paymentProviderUnavailable(
      final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.PAYMENT_PROVIDER_UNAVAILABLE, attributes);
  }

  /** The payment provider refused the payment. */
  static String paymentRefused(final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(step, CheckoutStepNotice.PAYMENT_REFUSED, attributes);
  }

  /** The order has nothing to pay. */
  static String nothingToPay(final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(step, CheckoutStepNotice.NOTHING_TO_PAY, attributes);
  }

  /**
   * A price changed since the review: the review the redirect leads to shows the order as it stands
   * today, naming the product whose price changed.
   */
  static String priceChangedSinceReview(
      final CheckoutStep step, final String productName, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.PRICE_CHANGED_SINCE_REVIEW, productName, attributes);
  }

  /**
   * The order changed since the review: the review the redirect leads to shows the order as it
   * stands today.
   */
  static String orderChangedSinceReview(
      final CheckoutStep step, final RedirectAttributes attributes) {
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.ORDER_CHANGED_SINCE_REVIEW, attributes);
  }

  /**
   * A line no longer passes, or its stock ran short: the line unavailable or short of stock is
   * named, whichever check found it.
   */
  static String stockRanShort(
      final CheckoutStep step,
      final CheckoutValidationResult validation,
      final RedirectAttributes attributes) {
    final ValidationError shortOfStock = validation.shortOfStock().orElseThrow();
    return CheckoutRoutes.redirectTo(
        step, CheckoutStepNotice.STOCK_RAN_SHORT, shortOfStock.productName(), attributes);
  }

  /**
   * The redirect for a "Place Order" not sent from the review: to the review, asking for it — or,
   * while an earlier step is still missing, to that step, saying so.
   *
   * @param review the domain's decision for the review step
   * @param attributes the redirect's attributes, which take the notice
   * @return the redirect to the review or to the step still open
   */
  static String askForTheReview(final StepAccess review, final RedirectAttributes attributes) {
    return review.granted()
        ? CheckoutRoutes.redirectTo(
            CheckoutStep.REVIEW, CheckoutStepNotice.REVIEW_FIRST, attributes)
        : CheckoutRoutes.redirectFor(review, CheckoutStepNotice.EARLIER_STEPS_FIRST, attributes);
  }

  /**
   * A submission sent too early goes to the step the domain sends the customer to — the step itself
   * would only pass them on, and a denied step passes no notice on.
   */
  private static String sentTooEarly(
      final CheckoutStep step,
      final CheckoutStepNotice notice,
      final Supplier<StepAccess> access,
      final RedirectAttributes attributes) {
    final StepAccess stillOpen = access.get();
    if (!stillOpen.granted()) {
      return CheckoutRoutes.redirectFor(stillOpen, notice, attributes);
    }
    return CheckoutRoutes.redirectTo(step, notice, attributes);
  }

  /**
   * The notice for what is still thrown: chosen by the step it met. The failure's message is for
   * the log, never for the customer.
   */
  private static CheckoutStepNotice residueNoticeFor(
      final CheckoutStep step, final RuntimeException residue) {
    return residue instanceof IllegalArgumentException ? invalidInputOn(step) : notSavedOn(step);
  }

  private static CheckoutStepNotice invalidInputOn(final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> CheckoutStepNotice.INVALID_BUYER_DETAILS;
      case DELIVERY -> CheckoutStepNotice.INVALID_DELIVERY_ADDRESS;
      case PAYMENT -> CheckoutStepNotice.PAYMENT_METHOD_MISSING;
      case REVIEW, CONFIRMATION -> CheckoutStepNotice.ORDER_NOT_PLACED;
    };
  }

  private static CheckoutStepNotice notSavedOn(final CheckoutStep step) {
    return switch (step) {
      case BUYER_INFO -> CheckoutStepNotice.BUYER_DETAILS_NOT_SAVED;
      case DELIVERY -> CheckoutStepNotice.DELIVERY_NOT_SAVED;
      case PAYMENT -> CheckoutStepNotice.PAYMENT_NOT_SAVED;
      case REVIEW, CONFIRMATION -> CheckoutStepNotice.ORDER_NOT_PLACED;
    };
  }
}
