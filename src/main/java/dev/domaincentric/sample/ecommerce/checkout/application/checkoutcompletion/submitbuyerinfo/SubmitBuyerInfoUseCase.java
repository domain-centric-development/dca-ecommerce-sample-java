package dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
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
    // Load session
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final CheckoutSession session =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()));

    // Create buyer info value object
    final BuyerInfo buyerInfo =
        BuyerInfo.of(command.email(), command.firstName(), command.lastName(), command.phone());

    // Submit buyer info (domain validates session state and step)
    session.submitBuyerInfo(buyerInfo);

    // Save session
    checkoutSessionRepository.save(session);

    eventPublisher.publishAndClearEvents(session);

    // Map to response
    return mapToResponse(session);
  }

  private SubmitBuyerInfoResult mapToResponse(final CheckoutSession session) {
    final BuyerInfo buyerInfo = session.buyerInfo();
    return new SubmitBuyerInfoResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        buyerInfo.email(),
        buyerInfo.firstName(),
        buyerInfo.lastName(),
        buyerInfo.phone());
  }
}
