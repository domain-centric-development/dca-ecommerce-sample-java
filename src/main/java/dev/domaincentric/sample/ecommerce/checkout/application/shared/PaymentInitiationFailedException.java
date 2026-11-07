package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.OutputPortException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.io.Serial;

/**
 * Raised when the payment provider answered and refused to open a payment for this checkout.
 *
 * <p>Part of the {@link PaymentProvider} contract: the adapter raises it, the use case catches it
 * and answers with a case of its result. The provider's own reason is carried through unchanged for
 * logs; the shop has no way to judge it, and the page shows the customer a fixed message of its own
 * instead.
 */
public final class PaymentInitiationFailedException extends OutputPortException {

  @Serial private static final long serialVersionUID = 1L;

  private final PaymentProviderId providerId;

  public PaymentInitiationFailedException(final PaymentProviderId providerId, final String reason) {
    super(reason);
    this.providerId = providerId;
  }

  public PaymentProviderId providerId() {
    return providerId;
  }
}
