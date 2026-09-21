package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when the items of a checkout no longer pass their own validation at confirmation time.
 *
 * <p>Prices move and stock runs out while a customer fills in the steps. Confirmation re-checks the
 * line items against the facts of that moment, and this exception carries what failed, item by
 * item, so the customer sees which position to change rather than a bare refusal.
 */
public final class CheckoutValidationException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutValidationResult validation;

  public CheckoutValidationException(final CheckoutValidationResult validation) {
    super("Checkout validation failed: " + validation.errors());
    this.validation = validation;
  }

  public CheckoutValidationResult validation() {
    return validation;
  }
}
