package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentInitiationFailedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutNotModifiableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.NothingToPayException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.ui.Model;

/**
 * What every checkout step page does the same way: it opens only when the domain grants the step,
 * and it turns a refused submission into the redirect back to the step with its notice.
 *
 * <p>A refusal is translated here, once: the step controllers call {@link #submitting} with their
 * use-case call and never catch a refusal themselves. What a refusal looks like to the customer is
 * decided in {@link #noticeFor}.
 */
public final class CheckoutStepPage {

  private CheckoutStepPage() {}

  /** A step's submission: the use-case call and the redirect it leads to when accepted. */
  @FunctionalInterface
  public interface Submission {
    /**
     * Submits the step.
     *
     * @return the redirect the accepted submission leads to
     */
    String submit();
  }

  /**
   * The redirect for a step the domain did not grant, or nothing for a granted one — which then
   * shows the notice the request carried as its error.
   *
   * @param access the domain's decision for the step
   * @param noticeCode the code of the notice a redirect brought along, if any
   * @param count the number that notice names, if any
   * @param model the page's model
   * @return the redirect to the step or the cart shown instead, or empty when the step opens
   */
  public static Optional<String> redirectIfDenied(
      final StepAccess access,
      final @Nullable String noticeCode,
      final @Nullable Integer count,
      final Model model) {
    final Optional<CheckoutStepNotice> notice = CheckoutStepNotice.fromCode(noticeCode);
    if (!access.granted()) {
      return Optional.of(
          notice
              .map(n -> CheckoutRoutes.redirectFor(access, n))
              .orElseGet(() -> CheckoutRoutes.redirectFor(access)));
    }
    notice.flatMap(n -> n.sentence(count)).ifPresent(text -> model.addAttribute("error", text));
    return Optional.empty();
  }

  /**
   * Submits a step, and turns a refusal into the redirect back to that step with its notice.
   *
   * @param step the step the submission belongs to
   * @param submission the use-case call
   * @return the redirect the submission leads to, or back to the step with the refusal's notice
   */
  public static String submitting(final CheckoutStep step, final Submission submission) {
    try {
      return submission.submit();
    } catch (UseCaseException | DomainException | IllegalArgumentException refusal) {
      // The two base types are the checkout's own refusals. IllegalArgumentException is still
      // here because a form field the customer typed reaches a value object unvalidated; each
      // field moved into request validation is one reason less to catch it.
      return redirectFor(step, refusal);
    }
  }

  /**
   * The redirect back to a step for a refusal of its submission, carrying the notice the step shows
   * and the number it names.
   */
  private static String redirectFor(final CheckoutStep step, final RuntimeException refusal) {
    if (step == CheckoutStep.REVIEW && refusal instanceof CheckoutValidationException invalid) {
      return CheckoutRoutes.redirectTo(
          step, CheckoutStepNotice.ITEMS_UNAVAILABLE, invalid.validation().errors().size());
    }
    return CheckoutRoutes.redirectTo(step, noticeFor(step, refusal));
  }

  /**
   * The notice for a refusal: chosen by the failure's type, and for a malformed or unexpected one
   * by the step it met. The failure's message is for the log, never for the customer.
   */
  static CheckoutStepNotice noticeFor(final CheckoutStep step, final RuntimeException refusal) {
    return switch (refusal) {
      case CheckoutStepNotCompletedException e -> CheckoutStepNotice.EARLIER_STEPS_FIRST;
      case CheckoutStepOutOfOrderException e ->
          step == CheckoutStep.REVIEW
              ? CheckoutStepNotice.REVIEW_FIRST
              : CheckoutStepNotice.EARLIER_STEPS_FIRST;
      case CheckoutSessionNotFoundException e -> CheckoutStepNotice.SESSION_NOT_FOUND;
      case CheckoutNotModifiableException e -> CheckoutStepNotice.CHECKOUT_NOT_MODIFIABLE;
      case PaymentInitiationFailedException e -> CheckoutStepNotice.PAYMENT_REFUSED;
      case PaymentProviderUnavailableException e -> CheckoutStepNotice.PAYMENT_PROVIDER_UNAVAILABLE;
      case PaymentProviderNotFoundException e -> CheckoutStepNotice.PAYMENT_METHOD_NOT_OFFERED;
      case NothingToPayException e -> CheckoutStepNotice.NOTHING_TO_PAY;
      case IllegalArgumentException e -> invalidInputOn(step);
      default -> notSavedOn(step);
    };
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
