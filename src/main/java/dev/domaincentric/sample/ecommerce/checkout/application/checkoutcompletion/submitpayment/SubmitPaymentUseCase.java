package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentInitiationFailedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for submitting payment information during checkout.
 *
 * <p>This use case handles the payment step by:
 *
 * <ul>
 *   <li>Loading and validating the checkout session
 *   <li>Validating the payment provider exists
 *   <li>Keeping a payment the provider already authorized for the same total
 *   <li>Otherwise validating the provider is available and cancelling a payment it authorized for
 *       another total
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

  private static final Logger LOG = LoggerFactory.getLogger(SubmitPaymentUseCase.class);

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

    // Provider lookup and payment initiation are remote-capable (payment service provider) -
    // both stay outside the transaction
    final PaymentProvider provider =
        paymentProviderRegistry
            .findById(providerId)
            .orElseThrow(() -> new PaymentProviderNotFoundException(providerId));

    // Everything the session itself can refuse is refused here, before the provider is reached: a
    // payment intent must not exist for a checkout that cannot accept it. The amount to charge is
    // the session total as it stands at this moment.
    final CheckoutSession snapshot =
        checkoutSessionRepository
            .findByIdForCustomer(sessionId, customerId)
            .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));
    snapshot.assertReadyForPayment();

    // A payment the provider already authorized for exactly this total stands: no second request,
    // so nothing is asked of the provider and its availability does not matter
    if (snapshot.authorizedPaymentCovers(providerId)) {
      return SubmitPaymentResult.kept(
          changeSession(
              sessionId, customerId, session -> session.keepAuthorizedPayment(providerId)));
    }

    if (!provider.isAvailable()) {
      throw new PaymentProviderUnavailableException(providerId);
    }

    cancelEarlierPayment(sessionId, customerId, snapshot);

    final Money amount = snapshot.totals().total();
    final PaymentProvider.PaymentResult initiation = provider.initiatePayment(sessionId, amount);
    if (initiation.outcome() == PaymentProvider.PaymentResult.Outcome.UNAVAILABLE) {
      throw new PaymentProviderUnavailableException(providerId);
    }
    if (!initiation.success()) {
      throw new PaymentInitiationFailedException(providerId, initiation.errorMessage());
    }

    final PaymentSelection paymentSelection =
        PaymentSelection.authorized(providerId, initiation.providerReference(), amount);

    // Short transaction: load, submit, save, publish. The session can still have moved on between
    // the check above and this load — a concurrent confirmation, an expiry — so the intent that is
    // already at the provider is released rather than left dangling.
    try {
      return SubmitPaymentResult.from(
          changeSession(sessionId, customerId, session -> session.submitPayment(paymentSelection)));
    } catch (final Throwable t) {
      // Every way out of the transaction releases the intent, an Error included: the clean-up is
      // one call that swallows its own failures, and leaving a payment behind is worse than
      // attempting it while the JVM is in trouble. The .NET twin catches as widely.
      cancelQuietly(provider, initiation.providerReference());
      throw t;
    }
  }

  /**
   * Short transaction: load the session, change it, save it, publish its events.
   *
   * @return the changed session
   */
  private CheckoutSession changeSession(
      final CheckoutSessionId sessionId,
      final CustomerId customerId,
      final Consumer<CheckoutSession> change) {
    return transactionBoundary.inTransaction(
        () -> {
          final CheckoutSession session =
              checkoutSessionRepository
                  .findByIdForCustomer(sessionId, customerId)
                  .orElseThrow(() -> new CheckoutSessionNotFoundException(sessionId));
          change.accept(session);
          checkoutSessionRepository.save(session);
          eventPublisher.publishAndClearEvents(session);
          return session;
        });
  }

  /**
   * Cancels a payment the provider authorized for another total before the new one is requested, so
   * the provider never holds two payments for one checkout. A payment the provider released is
   * released in the session too, so it covers nothing should the new request fail.
   */
  private void cancelEarlierPayment(
      final CheckoutSessionId sessionId,
      final CustomerId customerId,
      final CheckoutSession snapshot) {
    final PaymentSelection earlier = snapshot.paymentSelection();
    if (earlier == null || !earlier.hasReference()) {
      return;
    }
    final String reference = earlier.providerReference();
    final boolean released =
        paymentProviderRegistry
            .findById(earlier.providerId())
            .map(earlierProvider -> cancelQuietly(earlierProvider, reference))
            .orElse(false);
    if (released) {
      changeSession(sessionId, customerId, session -> session.releasePayment(reference));
    }
  }

  /**
   * Releases a payment intent the session could not accept, or one the session no longer needs. A
   * provider that refuses the cancellation is logged and changes nothing else: the caller is told
   * why their payment was rejected, not that the clean-up of it failed as well, and a payment for a
   * changed total is still requested.
   *
   * @return whether the provider released the payment
   */
  private boolean cancelQuietly(final PaymentProvider provider, final String providerReference) {
    try {
      final PaymentProvider.PaymentResult cancellation = provider.cancelPayment(providerReference);
      if (!cancellation.success()) {
        LOG.warn(
            "Payment intent {} could not be released: {}",
            providerReference,
            cancellation.errorMessage());
      }
      return cancellation.success();
    } catch (final RuntimeException e) {
      LOG.warn("Payment intent {} could not be released", providerReference, e);
      return false;
    }
  }
}
