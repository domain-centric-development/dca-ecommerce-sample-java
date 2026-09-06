package dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession;

import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import org.jspecify.annotations.Nullable;

/**
 * Output model containing checkout session data for display.
 *
 * <p>This result wraps a {@link CheckoutCartSnapshot} read model directly, providing access to all
 * checkout session state through the snapshot.
 *
 * @param found whether the session was found
 * @param session the checkout cart snapshot (null if not found)
 */
public record GetCheckoutSessionResult(boolean found, @Nullable CheckoutCartSnapshot session) {

  /**
   * Creates a not-found response.
   *
   * @return a response indicating the session was not found
   */
  public static GetCheckoutSessionResult notFound() {
    return new GetCheckoutSessionResult(false, null);
  }

  /**
   * Creates a found response with the checkout session snapshot.
   *
   * @param session the checkout cart snapshot
   * @return a response containing the session
   */
  public static GetCheckoutSessionResult found(final CheckoutCartSnapshot session) {
    return new GetCheckoutSessionResult(true, session);
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
