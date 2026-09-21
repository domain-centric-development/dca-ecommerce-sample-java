package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import java.io.Serial;

/**
 * Raised when the selected payment provider is known but cannot take a payment right now.
 *
 * <p>Different from an unknown provider: the choice was valid, the outside world is not ready, and
 * trying again later is the sensible answer.
 */
public final class PaymentProviderUnavailableException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final String providerId;

  public PaymentProviderUnavailableException(final String providerId) {
    super("Payment provider is currently unavailable: " + providerId);
    this.providerId = providerId;
  }

  public String providerId() {
    return providerId;
  }
}
