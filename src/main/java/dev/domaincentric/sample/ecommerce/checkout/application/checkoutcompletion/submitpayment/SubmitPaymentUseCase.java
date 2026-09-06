package dev.domaincentric.sample.ecommerce.checkout.application.submitpayment;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import org.springframework.stereotype.Service;

/**
 * Use case for submitting payment information during checkout.
 *
 * <p>This use case handles the payment step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Validating the payment provider exists and is available
 *   <li>Initiating the payment with the provider and taking its reference
 *   <li>Calling the domain method to submit payment info
 *   <li>Persisting the updated session
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SubmitPaymentInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class SubmitPaymentUseCase implements SubmitPaymentInputPort {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final PaymentProviderRegistry paymentProviderRegistry;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public SubmitPaymentUseCase(
      final CheckoutSessionRepository checkoutSessionRepository,
      final PaymentProviderRegistry paymentProviderRegistry,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.checkoutSessionRepository = checkoutSessionRepository;
    this.paymentProviderRegistry = paymentProviderRegistry;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public SubmitPaymentResult execute(final SubmitPaymentCommand command) {
    final CheckoutSessionId sessionId = CheckoutSessionId.of(command.sessionId());
    final PaymentProviderId providerId = PaymentProviderId.of(command.providerId());

    // Provider lookup and payment initiation are remote-capable (payment service provider) -
    // both stay outside the transaction
    final PaymentProvider provider =
        paymentProviderRegistry
            .findById(providerId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Payment provider not found: " + command.providerId()));
    if (!provider.isAvailable()) {
      throw new IllegalStateException(
          "Payment provider is currently unavailable: " + command.providerId());
    }

    // The amount to charge is the session total as it stands when payment is submitted
    final Money amount =
        checkoutSessionRepository
            .findById(sessionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Session not found: " + command.sessionId()))
            .totals()
            .total();

    final PaymentProvider.PaymentResult initiation = provider.initiatePayment(sessionId, amount);
    if (!initiation.success()) {
      throw new IllegalStateException(initiation.errorMessage());
    }

    final PaymentSelection paymentSelection =
        PaymentSelection.of(providerId, initiation.providerReference());

    // Short transaction: load, submit, save, publish
    return transactionBoundary.inTransaction(
        () -> {
          final CheckoutSession session =
              checkoutSessionRepository
                  .findById(sessionId)
                  .orElseThrow(
                      () ->
                          new IllegalArgumentException(
                              "Session not found: " + command.sessionId()));
          session.submitPayment(paymentSelection);
          checkoutSessionRepository.save(session);
          eventPublisher.publishAndClearEvents(session);
          return mapToResponse(session, provider);
        });
  }

  private SubmitPaymentResult mapToResponse(
      final CheckoutSession session, final PaymentProvider provider) {
    final PaymentSelection payment = session.paymentSelection();
    return new SubmitPaymentResult(
        session.id().value().toString(),
        session.currentStep().name(),
        session.status().name(),
        payment.providerId().value(),
        provider.displayName(),
        payment.providerReference());
  }
}
