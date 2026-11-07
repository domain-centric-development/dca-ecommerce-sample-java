package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.CheckoutNotModifiable;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.CheckoutSessionNotFound;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.CheckoutStepNotCompleted;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.CheckoutStepOutOfOrder;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.NothingToPay;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.PaymentProviderNotFound;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult.Submitted;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutNotModifiableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.NothingToPayException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import java.util.Optional;
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
 * requested when the order is placed at the review. Every refusal is decided before the session is
 * saved and returned as a case of {@link SubmitPaymentResult}.
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
      return new PaymentProviderNotFound(providerId);
    }

    // Short transaction: load, record the choice, save, publish
    return transactionBoundary.inTransaction(
        () -> {
          final Optional<CheckoutSession> found =
              checkoutSessionRepository.findByIdForCustomer(sessionId, customerId);
          if (found.isEmpty()) {
            return new CheckoutSessionNotFound(sessionId);
          }
          final CheckoutSession session = found.get();
          try {
            session.submitPayment(PaymentSelection.of(providerId));
          } catch (final CheckoutNotModifiableException notModifiable) {
            return new CheckoutNotModifiable(notModifiable.sessionId(), notModifiable.status());
          } catch (final CheckoutStepNotCompletedException notCompleted) {
            return new CheckoutStepNotCompleted(notCompleted.sessionId(), notCompleted.step());
          } catch (final CheckoutStepOutOfOrderException outOfOrder) {
            return new CheckoutStepOutOfOrder(
                outOfOrder.sessionId(), outOfOrder.requested(), outOfOrder.current());
          } catch (final NothingToPayException nothingToPay) {
            return new NothingToPay(nothingToPay.sessionId());
          }
          checkoutSessionRepository.save(session);
          eventPublisher.publishAndClearEvents(session);
          return Submitted.from(session);
        });
  }
}
