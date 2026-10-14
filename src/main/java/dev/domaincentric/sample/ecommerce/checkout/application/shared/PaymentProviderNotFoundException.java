package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.io.Serial;

/**
 * Raised when the payment provider a customer selected is not one the shop offers.
 *
 * <p>The registry is the shop's list of providers; an identifier outside it names nothing this
 * checkout can charge with.
 */
public final class PaymentProviderNotFoundException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final PaymentProviderId providerId;

  public PaymentProviderNotFoundException(final PaymentProviderId providerId) {
    super("Payment provider not found: " + providerId.value());
    this.providerId = providerId;
  }

  public PaymentProviderId providerId() {
    return providerId;
  }
}
