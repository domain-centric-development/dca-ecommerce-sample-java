package dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.jspecify.annotations.Nullable;

/**
 * Output model containing checkout session data for display.
 *
 * <p>This result wraps a {@link CheckoutCartSnapshot} read model directly, providing access to all
 * checkout session state through the snapshot. When the query named a step, {@link #stepAccess()}
 * carries the domain's decision whether that step may be opened; the adapter only maps it to a
 * route.
 *
 * @param found whether the session was found
 * @param session the checkout cart snapshot (null if not found)
 * @param stepAccess the access decision for the requested step (null if no step was requested)
 */
public record GetCheckoutSessionResult(
    boolean found, @Nullable CheckoutCartSnapshot session, @Nullable StepAccess stepAccess) {

  /**
   * Creates a not-found response.
   *
   * @return a response indicating the session was not found
   */
  public static GetCheckoutSessionResult notFound() {
    return new GetCheckoutSessionResult(false, null, null);
  }

  /**
   * Creates a not-found response for a caller that wanted to open a step: there is no checkout to
   * continue, so the customer goes back to the cart.
   *
   * @return a response indicating the session was not found
   */
  public static GetCheckoutSessionResult notFoundForStep() {
    return new GetCheckoutSessionResult(false, null, StepAccess.backToCart());
  }

  /**
   * Creates a found response with the checkout session snapshot.
   *
   * @param session the checkout cart snapshot
   * @return a response containing the session
   */
  public static GetCheckoutSessionResult found(final CheckoutCartSnapshot session) {
    return new GetCheckoutSessionResult(true, session, null);
  }

  /**
   * Creates a found response with the snapshot and the access decision for the requested step.
   *
   * @param session the checkout cart snapshot
   * @param stepAccess whether the requested step may be opened
   * @return a response containing the session and the decision
   */
  public static GetCheckoutSessionResult found(
      final CheckoutCartSnapshot session, final StepAccess stepAccess) {
    return new GetCheckoutSessionResult(true, session, stepAccess);
  }

  /**
   * Convenience method to get the current step as a string.
   *
   * @return the current step name, or null if not found
   */
  public @Nullable String currentStep() {
    return session != null ? session.step().name() : null;
  }

  /**
   * Convenience method to get the status as a string.
   *
   * @return the status name, or null if not found
   */
  public @Nullable String status() {
    return session != null ? session.status().name() : null;
  }
}
