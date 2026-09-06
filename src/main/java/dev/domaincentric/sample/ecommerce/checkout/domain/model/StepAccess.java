package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import org.jspecify.annotations.Nullable;

/**
 * Value Object answering whether a checkout step may be opened.
 *
 * <p>Three outcomes: access is granted; the customer belongs on another step of the same checkout
 * ({@link #redirectStep()}); or there is no checkout to continue and the customer goes back to the
 * cart ({@link #isBackToCart()}). The web adapter turns the outcome into a route — the domain knows
 * steps, not URLs.
 *
 * @param granted whether the requested step may be shown
 * @param redirectStep the step to show instead, null when granted or when the cart is the target
 */
public record StepAccess(boolean granted, @Nullable CheckoutStep redirectStep) implements Value {

  public StepAccess {
    if (granted && redirectStep != null) {
      throw new IllegalArgumentException("A granted access names no redirect step");
    }
  }

  /** The requested step may be opened. */
  public static StepAccess grant() {
    return new StepAccess(true, null);
  }

  /** The customer belongs on another step of this checkout. */
  public static StepAccess redirectTo(final CheckoutStep step) {
    if (step == null) {
      throw new IllegalArgumentException("Redirect step cannot be null");
    }
    return new StepAccess(false, step);
  }

  /** There is no checkout to continue — the customer starts over from the cart. */
  public static StepAccess backToCart() {
    return new StepAccess(false, null);
  }

  /** Whether the outcome sends the customer back to the cart rather than to another step. */
  public boolean isBackToCart() {
    return !granted && redirectStep == null;
  }
}
