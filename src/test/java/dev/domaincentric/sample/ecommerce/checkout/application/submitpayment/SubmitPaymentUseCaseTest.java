package dev.domaincentric.sample.ecommerce.checkout.application.submitpayment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.dca.spring.InMemoryTransactionBoundary;
import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence.InMemoryCheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentUseCase;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the payment provider is told at the payment step: nothing. The step only chooses how to pay;
 * the payment is requested when the order is placed.
 */
class SubmitPaymentUseCaseTest {

  private static final String CUSTOMER = "customer-1";

  private final InMemoryCheckoutSessionRepository sessions =
      new InMemoryCheckoutSessionRepository();
  private final RecordingPaymentProvider provider = new RecordingPaymentProvider();
  private final SilentEventPublisher events = new SilentEventPublisher();
  private final SubmitPaymentUseCase useCase =
      new SubmitPaymentUseCase(
          sessions,
          new SingleProviderRegistry(provider),
          events,
          new InMemoryTransactionBoundary());

  @Test
  @DisplayName("A session without a delivery address never reaches the provider")
  void anIncompleteSessionNeverReachesTheProvider() {
    final CheckoutSession session = sessionWithBuyerInfoOnly();

    assertThrows(
        CheckoutStepNotCompletedException.class,
        () -> useCase.execute(new SubmitPaymentCommand(session.id().value(), CUSTOMER, "mock")));

    assertTrue(
        provider.initiations.isEmpty(),
        "a payment intent must not exist for a checkout that cannot accept it");
  }

  @Test
  @DisplayName("A session that is ready moves to the review without a payment request")
  void aReadySessionMovesToTheReviewWithoutAPaymentRequest() {
    final CheckoutSession session = readySession();

    final SubmitPaymentResult result =
        useCase.execute(new SubmitPaymentCommand(session.id().value(), CUSTOMER, "mock"));

    assertEquals(CheckoutStep.REVIEW.name(), result.currentStep());
    assertTrue(provider.initiations.isEmpty(), "the payment step only chooses how to pay");
  }

  private CheckoutSession sessionWithBuyerInfoOnly() {
    final CheckoutSession session = startedSession();
    session.submitBuyerInfo(BuyerInfo.of("ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    return sessions.save(session);
  }

  private CheckoutSession readySession() {
    final CheckoutSession session = sessionWithBuyerInfoOnly();
    session.submitDelivery(
        DeliveryAddress.of("123 Main Street", "Springfield", "12345", "United States"),
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", Money.euro(5)));
    return sessions.save(session);
  }

  private CheckoutSession startedSession() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(), ProductId.generate(), "Thing", Money.euro(10), 1, null);
    return CheckoutSession.start(
        CartId.generate(), CustomerId.of(CUSTOMER), List.of(item), Money.euro(10));
  }

  /** A provider that remembers what it was asked to do. */
  private static final class RecordingPaymentProvider implements PaymentProvider {

    private final List<String> initiations = new ArrayList<>();
    private final List<String> cancellations = new ArrayList<>();

    @Override
    public PaymentProviderId providerId() {
      return PaymentProviderId.of("mock");
    }

    @Override
    public String displayName() {
      return "Recording provider";
    }

    @Override
    public PaymentResult initiatePayment(final CheckoutSessionId sessionId, final Money amount) {
      final String reference = "intent-" + initiations.size();
      initiations.add(reference);
      return PaymentResult.success(reference);
    }

    @Override
    public PaymentResult confirmPayment(final String providerReference) {
      return PaymentResult.success(providerReference);
    }

    @Override
    public PaymentResult cancelPayment(final String providerReference) {
      cancellations.add(providerReference);
      return PaymentResult.success(providerReference);
    }

    @Override
    public boolean isAvailable() {
      return true;
    }
  }

  /** The only provider this shop knows in the test. */
  private record SingleProviderRegistry(PaymentProvider provider)
      implements PaymentProviderRegistry {

    @Override
    public Optional<PaymentProvider> findById(final PaymentProviderId providerId) {
      return provider.providerId().equals(providerId) ? Optional.of(provider) : Optional.empty();
    }

    @Override
    public List<PaymentProvider> findAll() {
      return List.of(provider);
    }

    @Override
    public List<PaymentProvider> findAvailable() {
      return provider.isAvailable() ? List.of(provider) : List.of();
    }
  }

  private static final class SilentEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(final DomainEvent event) {
      // the events of this use case are not what is under test
    }

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      aggregate.clearDomainEvents();
    }
  }
}
