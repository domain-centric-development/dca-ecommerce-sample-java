package dev.domaincentric.sample.ecommerce.checkout.application.getpaymentproviders;

import java.util.List;

/**
 * Output model containing available payment providers.
 *
 * @param paymentProviders list of available payment providers
 */
public record GetPaymentProvidersResult(List<PaymentProviderData> paymentProviders) {

  /**
   * Individual payment provider details.
   *
   * @param id the payment provider identifier
   * @param displayName the human-readable display name
   * @param available whether the provider is currently available
   */
  public record PaymentProviderData(String id, String displayName, boolean available) {}
}
