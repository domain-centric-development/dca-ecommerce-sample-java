package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import java.util.List;
import java.util.Optional;

/**
 * Output port for looking up payment providers.
 *
 * <p>The checkout discovers and selects {@link PaymentProvider}s at runtime through this port.
 * Which providers exist is the adapter's business — it fills its own list from configuration or
 * discovery — so the port carries lookups only, not registration.
 */
public interface PaymentProviderRegistry extends OutputPort {

  /**
   * Finds a payment provider by its unique identifier.
   *
   * @param providerId the payment provider ID to look up
   * @return the payment provider if found, empty otherwise
   */
  Optional<PaymentProvider> findById(PaymentProviderId providerId);

  /**
   * Returns all registered payment providers.
   *
   * @return list of all registered providers (never null, may be empty)
   */
  List<PaymentProvider> findAll();

  /**
   * Returns all payment providers that are currently available for processing.
   *
   * <p>This filters out providers that are temporarily unavailable (e.g., due to maintenance or
   * configuration issues).
   *
   * @return list of available providers (never null, may be empty)
   */
  List<PaymentProvider> findAvailable();
}
