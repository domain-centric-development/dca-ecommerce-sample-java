package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ShippingOptionNotOfferedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ShippingOptionRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import org.springframework.stereotype.Service;

/**
 * Use case for submitting delivery information during checkout.
 *
 * <p>This use case handles the delivery step by:
 *
 * <ul>
 *   <li>Taking the chosen ShippingOption, its price included, from the shop's offered options — an
 *       option the shop does not offer is refused with {@link ShippingOptionNotOfferedException}
 *   <li>Loading and validating the checkout session
 *   <li>Creating the DeliveryAddress value object from command data
 *   <li>Calling the domain method to submit delivery info
 *   <li>Persisting the updated session
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitDeliveryInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class SubmitDeliveryUseCase implements SubmitDeliveryInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final ShippingOptionRegistry shippingOptionRegistry;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public SubmitDeliveryUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final ShippingOptionRegistry shippingOptionRegistry,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.shippingOptionRegistry = shippingOptionRegistry;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public SubmitDeliveryResult execute(final SubmitDeliveryCommand command) {
    // The shipping option as the shop offers it, its price the shop's and not the caller's. The
    // registry may leave the process, so it is asked before the transaction
    final ShippingOption shippingOption =
        shippingOptionRegistry
            .findById(command.shippingOptionId())
            .orElseThrow(() -> new ShippingOptionNotOfferedException(command.shippingOptionId()));

    return transactionBoundary.inTransaction(
        () -> {
          // Load the session as the caller's own: a session that is not theirs is not found
          final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
          final CheckoutSession session =
              checkoutSessionRepository
                  .findByIdForCustomer(sessionId, CustomerId.of(command.customerId()))
                  .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));

          final DeliveryAddress address =
              DeliveryAddress.of(
                  command.street(),
                  command.streetLine2(),
                  command.city(),
                  command.postalCode(),
                  command.country(),
                  command.state());

          // Submit delivery (domain validates session state and step)
          session.submitDelivery(address, shippingOption);
          checkoutSessionRepository.save(session);
          eventPublisher.publishAndClearEvents(session);
          return SubmitDeliveryResult.from(session);
        });
  }
}
