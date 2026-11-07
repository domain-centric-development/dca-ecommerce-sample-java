package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoResult.CheckoutNotModifiable;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoResult.CheckoutSessionNotFound;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoResult.CheckoutStepOutOfOrder;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoResult.Submitted;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutNotModifiableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for submitting buyer contact information during checkout.
 *
 * <p>This use case handles the buyer info step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Creating BuyerInfo value object from command data
 *   <li>Calling the domain method to submit buyer info
 *   <li>Persisting the updated session
 * </ul>
 *
 * <p>Every refusal — no such checkout, a session that takes no changes, a step out of order — is
 * decided before the session is saved and returned as a case of {@link SubmitBuyerInfoResult}.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitBuyerInfoInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SubmitBuyerInfoUseCase implements SubmitBuyerInfoInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final DomainEventPublisher eventPublisher;

  public SubmitBuyerInfoUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final DomainEventPublisher eventPublisher) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public SubmitBuyerInfoResult execute(final SubmitBuyerInfoCommand command) {
    // Load the session as the caller's own: a session that is not theirs is not found
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final Optional<CheckoutSession> found =
        checkoutSessionRepository.findByIdForCustomer(
            sessionId, CustomerId.of(command.customerId()));
    if (found.isEmpty()) {
      return new CheckoutSessionNotFound(sessionId);
    }
    final CheckoutSession session = found.get();

    // Create buyer info value object
    final BuyerInfo buyerInfo =
        BuyerInfo.of(command.email(), command.firstName(), command.lastName(), command.phone());

    // Submit buyer info (domain validates session state and step)
    try {
      session.submitBuyerInfo(buyerInfo);
    } catch (final CheckoutNotModifiableException notModifiable) {
      return new CheckoutNotModifiable(notModifiable.sessionId(), notModifiable.status());
    } catch (final CheckoutStepOutOfOrderException outOfOrder) {
      return new CheckoutStepOutOfOrder(
          outOfOrder.sessionId(), outOfOrder.requested(), outOfOrder.current());
    }

    // Save session
    checkoutSessionRepository.save(session);

    eventPublisher.publishAndClearEvents(session);

    return Submitted.from(session);
  }
}
