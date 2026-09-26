package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import java.io.Serial;

/**
 * Raised when the payment provider refused to open a payment for this checkout.
 *
 * <p>The provider's own reason is carried through unchanged for logs and callers; the shop has no
 * way to judge it. The payment page shows the customer a fixed message of its own instead.
 */
public final class PaymentInitiationFailedException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final String providerId;

  public PaymentInitiationFailedException(final String providerId, final String reason) {
    super(reason);
    this.providerId = providerId;
  }

  public String providerId() {
    return providerId;
  }
}
