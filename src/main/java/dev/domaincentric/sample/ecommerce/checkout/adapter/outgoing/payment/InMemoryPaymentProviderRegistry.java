package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of PaymentProviderRegistry.
 *
 * <p>This secondary adapter holds the payment providers in a map keyed by id. It is filled once,
 * from the PaymentProvider beans Spring's dependency injection discovers; the port exposes lookups
 * only.
 *
 * <p>In a production system, this implementation may be extended to support dynamic provider
 * configuration from a database or external configuration service.
 */
@Component
public class InMemoryPaymentProviderRegistry implements PaymentProviderRegistry {

  private final Map<PaymentProviderId, PaymentProvider> providers;

  /**
   * Creates a registry holding the given providers.
   *
   * <p>Spring injects all PaymentProvider beans, so the registry is populated at startup.
   *
   * @param availableProviders the payment providers to offer (may be empty)
   */
  public InMemoryPaymentProviderRegistry(final List<PaymentProvider> availableProviders) {
    this.providers =
        availableProviders.stream()
            .collect(
                Collectors.toUnmodifiableMap(PaymentProvider::providerId, provider -> provider));
  }

  @Override
  public Optional<PaymentProvider> findById(final PaymentProviderId providerId) {
    return Optional.ofNullable(providers.get(providerId));
  }

  @Override
  public List<PaymentProvider> findAll() {
    return List.copyOf(providers.values());
  }

  @Override
  public List<PaymentProvider> findAvailable() {
    return providers.values().stream().filter(PaymentProvider::isAvailable).toList();
  }
}
