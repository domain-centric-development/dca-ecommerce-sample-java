package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of PaymentProviderRegistry.
 *
 * <p>This secondary adapter provides a thread-safe registry for payment providers using
 * ConcurrentHashMap. It automatically registers any PaymentProvider beans discovered by Spring's
 * dependency injection.
 *
 * <p>In a production system, this implementation may be extended to support dynamic provider
 * configuration from a database or external configuration service.
 */
@Component
public class InMemoryPaymentProviderRegistry implements PaymentProviderRegistry {

  private final ConcurrentHashMap<PaymentProviderId, PaymentProvider> providers =
      new ConcurrentHashMap<>();

  /**
   * Creates a new registry with the given providers auto-registered.
   *
   * <p>Spring will inject all PaymentProvider beans, automatically populating the registry at
   * startup.
   *
   * @param availableProviders list of payment providers to register (may be empty)
   */
  public InMemoryPaymentProviderRegistry(final List<PaymentProvider> availableProviders) {
    if (availableProviders != null) {
      for (final PaymentProvider provider : availableProviders) {
        register(provider);
      }
    }
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

  @Override
  public void register(final PaymentProvider provider) {
    providers.put(provider.providerId(), provider);
  }

  @Override
  public boolean unregister(final PaymentProviderId providerId) {
    return providers.remove(providerId) != null;
  }

  @Override
  public boolean isRegistered(final PaymentProviderId providerId) {
    return providers.containsKey(providerId);
  }
}
