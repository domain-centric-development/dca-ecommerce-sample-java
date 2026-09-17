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
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentUseCase;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.TaxCalculator;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the payment provider is told, and when.
 *
 * <p>Initiating a payment is a remote effect: it exists at the provider whether or not this shop
 * goes on to accept it. So everything the session itself can refuse is refused before the call, and
 * an intent that the session turns out not to accept afterwards is released again.
 */
class SubmitPaymentUseCaseTest {

  private static final String CUSTOMER = "customer-1";

  private final InMemoryCheckoutSessionRepository sessions =
      new InMemoryCheckoutSessionRepository();
  private final RecordingPaymentProvider provider = new RecordingPaymentProvider();
  private final SubmitPaymentUseCase useCase =
      new SubmitPaymentUseCase(
          sessions,
          new SingleProviderRegistry(provider),
          new SilentEventPublisher(),
          new InMemoryTransactionBoundary());

  @Test
  @DisplayName("A session without a delivery address never reaches the provider")
  void anIncompleteSessionNeverReachesTheProvider() {
    final CheckoutSession session = sessionWithBuyerInfoOnly();

    assertThrows(
        IllegalStateException.class,
        () -> useCase.execute(new SubmitPaymentCommand(session.id().value(), CUSTOMER, "mock")));

    assertTrue(
        provider.initiations.isEmpty(),
        "a payment intent must not exist for a checkout that cannot accept it");
  }

  @Test
  @DisplayName("An intent the session turns out not to accept is released again")
  void anIntentTheSessionRejectsIsReleased() {
    final CheckoutSession session = readySession();

    // The session moves on while the provider is being called — here by being abandoned, in
    // production by a concurrent confirmation or an expiry.
    provider.duringInitiation =
        () -> {
          session.abandon();
          sessions.save(session);
        };

    assertThrows(
        IllegalStateException.class,
        () -> useCase.execute(new SubmitPaymentCommand(session.id().value(), CUSTOMER, "mock")));

    assertEquals(1, provider.initiations.size(), "the intent was created before the session moved");
    assertEquals(
        provider.initiations, provider.cancellations, "and exactly that intent was released");
  }

  @Test
  @DisplayName("A session that is ready keeps its intent")
  void aReadySessionKeepsItsIntent() {
    final CheckoutSession session = readySession();

    useCase.execute(new SubmitPaymentCommand(session.id().value(), CUSTOMER, "mock"));

    assertEquals(1, provider.initiations.size());
    assertTrue(provider.cancellations.isEmpty(), "a payment that was accepted stays");
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
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", Money.euro(5)),
        new TaxCalculator());
    return sessions.save(session);
  }

  private CheckoutSession startedSession() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(), ProductId.generate(), "Thing", Money.euro(10), 1, null);
    return CheckoutSession.start(
        CartId.generate(),
        CustomerId.of(CUSTOMER),
        List.of(item),
        Money.euro(10),
        new TaxCalculator());
  }

  /** A provider that remembers what it was asked to do. */
  private static final class RecordingPaymentProvider implements PaymentProvider {

    private final List<String> initiations = new ArrayList<>();
    private final List<String> cancellations = new ArrayList<>();

    /** What happens at the provider's end while the call is in flight. */
    private Runnable duringInitiation = () -> {};

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
      duringInitiation.run();
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
