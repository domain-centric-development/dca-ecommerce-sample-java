package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.OutputPortException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.io.Serial;

/**
 * Raised when the payment provider did not answer in time, could not be reached, or answered
 * outside its contract.
 *
 * <p>Part of the {@link PaymentProvider} contract: the provider is a foreign system, and its being
 * unreachable is expected behaviour of the integration. The adapter raises it, the use case catches
 * it and answers with a case of its result. Different from an unknown provider: the choice was
 * valid, the outside world is not ready, and trying again later is the sensible answer.
 */
public final class PaymentProviderUnavailableException extends OutputPortException {

  @Serial private static final long serialVersionUID = 1L;

  private final PaymentProviderId providerId;

  public PaymentProviderUnavailableException(
      final PaymentProviderId providerId, final String reason) {
    super(reason);
    this.providerId = providerId;
  }

  public PaymentProviderId providerId() {
    return providerId;
  }
}
