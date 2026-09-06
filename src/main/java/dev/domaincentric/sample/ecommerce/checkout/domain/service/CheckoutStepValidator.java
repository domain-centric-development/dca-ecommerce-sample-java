package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainService;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.jspecify.annotations.Nullable;

/**
 * Domain Service for validating checkout step navigation.
 *
 * <p>Enforces business rules for step access during checkout:
 *
 * <ul>
 *   <li>Cannot access steps if session is invalid (null)
 *   <li>Cannot skip ahead to steps without completing prerequisites
 *   <li>Can go back to previously completed steps
 *   <li>Cannot access steps in terminal session states
 *   <li>Can only access CONFIRMATION step when session is confirmed or completed
 * </ul>
 *
 * <p>Answers with a {@link StepAccess}: granted, another step of the same checkout, or back to the
 * cart. It decides on the {@link CheckoutCartSnapshot} read model — the aggregate never leaves the
 * application layer — and knows nothing about routes; the web adapter maps the answer to a URL.
 */
public final class CheckoutStepValidator implements DomainService {

  /**
   * Decides whether a specific checkout step may be opened.
   *
   * @param session the checkout session snapshot (may be null for invalid sessions)
   * @param targetStep the step the user wants to access
   * @return the access decision
   */
  public StepAccess accessTo(
      @Nullable final CheckoutCartSnapshot session, final CheckoutStep targetStep) {

    // Rule 1: Invalid session - back to the cart
    if (session == null) {
      return StepAccess.backToCart();
    }

    // Rule 2: Terminal states handling (COMPLETED, ABANDONED, EXPIRED)
    if (session.status().isTerminal()) {
      return handleTerminalState(session, targetStep);
    }

    // Rule 3: CONFIRMED state - can only access CONFIRMATION
    if (session.status().canComplete()) {
      return handleConfirmedState(targetStep);
    }

    // Rule 4: CONFIRMATION step is only accessible after checkout is confirmed or completed
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return handleConfirmationAccess(session);
    }

    // Rule 5: Cannot skip ahead - must complete prior steps
    if (isSkippingAhead(session, targetStep)) {
      return StepAccess.redirectTo(session.step());
    }

    return StepAccess.grant();
  }

  private StepAccess handleTerminalState(
      final CheckoutCartSnapshot session, final CheckoutStep targetStep) {

    return switch (session.status()) {
      case COMPLETED -> {
        // Completed sessions can only access CONFIRMATION
        if (targetStep == CheckoutStep.CONFIRMATION) {
          yield StepAccess.grant();
        }
        yield StepAccess.redirectTo(CheckoutStep.CONFIRMATION);
      }
      case ABANDONED, EXPIRED -> {
        // Abandoned/expired sessions send the customer back to the cart to start fresh
        yield StepAccess.backToCart();
      }
      // CONFIRMED is not terminal - handled separately by handleConfirmedState
      // ACTIVE is not terminal - handled by normal flow
      default -> StepAccess.grant();
    };
  }

  private StepAccess handleConfirmedState(final CheckoutStep targetStep) {
    // CONFIRMED sessions can only access CONFIRMATION step
    if (targetStep == CheckoutStep.CONFIRMATION) {
      return StepAccess.grant();
    }
    return StepAccess.redirectTo(CheckoutStep.CONFIRMATION);
  }

  private StepAccess handleConfirmationAccess(final CheckoutCartSnapshot session) {
    // CONFIRMATION is only accessible when status is CONFIRMED or COMPLETED
    if (session.isConfirmed() || session.isCompleted()) {
      return StepAccess.grant();
    }

    // Back to the current step if trying to access CONFIRMATION prematurely
    return StepAccess.redirectTo(session.step());
  }

  private boolean isSkippingAhead(
      final CheckoutCartSnapshot session, final CheckoutStep targetStep) {
    // Check if user is trying to access a step beyond their current progress
    final CheckoutStep currentStep = session.step();

    // Cannot go to a step that comes after the current step
    if (targetStep.isAfter(currentStep)) {
      return true;
    }

    // For steps before or equal to current, also verify prerequisites are met
    // (going back is allowed, but going to a step whose prerequisites aren't met is not)
    return !arePrerequisitesMet(session, targetStep);
  }

  private boolean arePrerequisitesMet(
      final CheckoutCartSnapshot session, final CheckoutStep targetStep) {
    return switch (targetStep) {
      case BUYER_INFO -> true; // First step, no prerequisites
      case DELIVERY -> session.isStepCompleted(CheckoutStep.BUYER_INFO);
      case PAYMENT ->
          session.isStepCompleted(CheckoutStep.BUYER_INFO)
              && session.isStepCompleted(CheckoutStep.DELIVERY);
      case REVIEW ->
          session.isStepCompleted(CheckoutStep.BUYER_INFO)
              && session.isStepCompleted(CheckoutStep.DELIVERY)
              && session.isStepCompleted(CheckoutStep.PAYMENT);
      case CONFIRMATION -> session.isCompleted();
    };
  }
}
