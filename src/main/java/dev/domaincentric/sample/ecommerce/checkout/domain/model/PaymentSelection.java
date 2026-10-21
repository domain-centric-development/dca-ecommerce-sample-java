package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing the selected payment method for checkout.
 *
 * <p>Contains the payment provider identifier, optional provider-specific reference data (e.g.,
 * payment intent ID, saved card token) and the amount the provider authorized the payment for.
 */
public record PaymentSelection(
    PaymentProviderId providerId,
    @Nullable String providerReference,
    @Nullable Money authorizedAmount)
    implements Value {

  public PaymentSelection {
    if (providerId == null) {
      throw new IllegalArgumentException("Payment provider ID cannot be null");
    }
  }

  public static PaymentSelection of(final PaymentProviderId providerId) {
    return new PaymentSelection(providerId, null, null);
  }

  public static PaymentSelection of(
      final PaymentProviderId providerId, final String providerReference) {
    return new PaymentSelection(providerId, providerReference, null);
  }

  /**
   * A payment the provider authorized for an amount.
   *
   * @param providerId the provider that authorized it
   * @param providerReference the payment reference the provider answered with
   * @param authorizedAmount the amount it authorized
   * @return the selection
   */
  public static PaymentSelection authorized(
      final PaymentProviderId providerId,
      final String providerReference,
      final Money authorizedAmount) {
    return new PaymentSelection(providerId, providerReference, authorizedAmount);
  }

  public boolean hasReference() {
    return providerReference != null && !providerReference.isBlank();
  }
}
