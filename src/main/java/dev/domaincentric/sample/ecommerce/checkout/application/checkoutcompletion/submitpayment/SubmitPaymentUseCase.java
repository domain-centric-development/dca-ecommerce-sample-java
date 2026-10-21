package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import org.springframework.stereotype.Service;

/**
 * Use case for submitting payment information during checkout.
 *
 * <p>This use case handles the payment step by:
 *
 * <ul>
 *   <li>Validating the payment provider exists
 *   <li>Loading and validating the checkout session
 *   <li>Calling the domain method to record the chosen provider
 *   <li>Persisting the updated session
 * </ul>
 *
 * <p>The step only chooses how to pay: nothing is requested from the provider here. The payment is
 * requested when the order is placed at the review.
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
    final CustomerId customerId = CustomerId.of(command.customerId());
    final PaymentProviderId providerId = PaymentProviderId.of(command.providerId());

    // Only a way to pay the shop offers can be chosen
    if (paymentProviderRegistry.findById(providerId).isEmpty()) {
      throw new PaymentProviderNotFoundException(providerId);
    }

    // Short transaction: load, submit, save, publish
    return transactionBoundary.inTransaction(
        () -> {
          final CheckoutSession session =
              checkoutSessionRepository
                  .findByIdForCustomer(sessionId, customerId)
                  .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));
          session.assertReadyForPayment();
          session.submitPayment(PaymentSelection.of(providerId));
          checkoutSessionRepository.save(session);
          eventPublisher.publishAndClearEvents(session);
          return SubmitPaymentResult.from(session);
        });
  }
}
