package dev.domaincentric.sample.ecommerce.checkout.application.submitdelivery;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for submitting delivery information during checkout.
 *
 * <p>This use case handles the delivery step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Creating DeliveryAddress and ShippingOption value objects from command data
 *   <li>Calling the domain method to submit delivery info
 *   <li>Persisting the updated session
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitDeliveryInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SubmitDeliveryUseCase implements SubmitDeliveryInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final TaxCalculator taxCalculator;
  private final DomainEventPublisher eventPublisher;

  public SubmitDeliveryUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final TaxCalculator taxCalculator,
      final DomainEventPublisher eventPublisher) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.taxCalculator = taxCalculator;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public SubmitDeliveryResult execute(final SubmitDeliveryCommand command) {
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Create delivery address value object
    final DeliveryAddress address =
        DeliveryAddress.of(
            command.street(),
            command.streetLine2(),
            command.city(),
            command.postalCode(),
            command.country(),
            command.state());

    // Create shipping option value object
    final Money shippingCost =
        Money.of(command.shippingCost(), Currency.getInstance(command.currencyCode()));
    final ShippingOption shippingOption =
        ShippingOption.of(
            command.shippingOptionId(),
            command.shippingOptionName(),
            command.estimatedDelivery(),
            shippingCost);

    // Submit delivery (domain validates session state and step)
    session.submitDelivery(address, shippingOption, taxCalculator);

    // Save session
    checkoutSessionRepository.save(session);

    eventPublisher.publishAndClearEvents(session);

    // Map to response
    return mapToResponse(session);
  }

  private SubmitDeliveryResult mapToResponse(final CheckoutSession session) {
    final DeliveryAddress address = session.deliveryAddress();
    final ShippingOption shipping = session.shippingOption();
    return new SubmitDeliveryResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        address.street(),
        address.streetLine2(),
        address.city(),
        address.postalCode(),
        address.country(),
        address.state(),
        shipping.id(),
        shipping.name(),
        shipping.estimatedDelivery(),
        shipping.cost().amount(),
        shipping.cost().currency().getCurrencyCode());
  }
}
