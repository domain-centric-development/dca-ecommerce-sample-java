package dev.domaincentric.sample.ecommerce.checkout.application.getpaymentproviders;

import dev.domaincentric.sample.ecommerce.checkout.application.getpaymentproviders.GetPaymentProvidersResult.PaymentProviderData;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving available payment providers.
 *
 * <p>This use case queries the {@link PaymentProviderRegistry} to get all registered payment
 * providers and their availability status.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetPaymentProvidersInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class GetPaymentProvidersUseCase implements GetPaymentProvidersInputPort {

  private final PaymentProviderRegistry paymentProviderRegistry;

  public GetPaymentProvidersUseCase(final PaymentProviderRegistry paymentProviderRegistry) {
    this.paymentProviderRegistry = paymentProviderRegistry;
  }

  @Override
  public GetPaymentProvidersResult execute(final GetPaymentProvidersQuery query) {
    final List<PaymentProviderData> providers =
        paymentProviderRegistry.findAll().stream().map(this::mapToData).toList();
    return new GetPaymentProvidersResult(providers);
  }

  private PaymentProviderData mapToData(final PaymentProvider provider) {
    return new PaymentProviderData(
        provider.providerId().value(), provider.displayName(), provider.isAvailable());
  }
}
