package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.dca.spring.InMemoryTransactionBoundary;
import dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.persistence.InMemoryCheckoutSessionRepository;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutArticleDataPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderRegistry;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.StockReservationPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutArticle;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionStatus;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentDiffersFromReviewException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutPricing;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Placing the order: the payment the session records is the one the provider the review showed
 * authorized.
 */
class ConfirmCheckoutUseCaseTest {

  private static final String CUSTOMER = "customer-1";
  private static final PaymentProviderId CHOSEN = PaymentProviderId.of("chosen");
  private static final ProductId THING = ProductId.generate();

  private final InMemoryCheckoutSessionRepository sessions =
      new InMemoryCheckoutSessionRepository();
  private final RecordingStockReservation stock = new RecordingStockReservation();

  @Test
  @DisplayName("A payment the chosen provider authorized places the order")
  void aPaymentTheChosenProviderAuthorizedPlacesTheOrder() {
    final CheckoutSession session = sessionAtTheReview();

    assertInstanceOf(
        ConfirmCheckoutResult.Confirmed.class,
        useCaseWith(new AuthorizingProvider(CHOSEN)).execute(commandFor(session)));

    final CheckoutSession placed = sessions.findById(session.id()).orElseThrow();
    assertEquals(CheckoutSessionStatus.CONFIRMED, placed.status());
    assertEquals(CHOSEN, placed.paymentSelection().providerId());
  }

  @Test
  @DisplayName("A payment another provider authorized is refused and gives the stock back")
  void aPaymentAnotherProviderAuthorizedIsRefused() {
    final CheckoutSession session = sessionAtTheReview();
    final ConfirmCheckoutUseCase useCase =
        useCaseWith(new AuthorizingProvider(PaymentProviderId.of("other")));
    final ConfirmCheckoutCommand command = commandFor(session);

    assertThrows(PaymentDiffersFromReviewException.class, () -> useCase.execute(command));

    final CheckoutSession unplaced = sessions.findById(session.id()).orElseThrow();
    assertEquals(CheckoutSessionStatus.ACTIVE, unplaced.status());
    assertFalse(unplaced.paymentSelection().hasReference(), "no payment is recorded");
    assertEquals(stock.reserved, stock.released, "the reservation is given back");
  }

  private ConfirmCheckoutUseCase useCaseWith(final PaymentProvider provider) {
    return new ConfirmCheckoutUseCase(
        sessions,
        new OneArticle(),
        new CheckoutPricing(),
        new SilentEventPublisher(),
        new InMemoryTransactionBoundary(),
        stock,
        new AnsweringRegistry(provider));
  }

  private static ConfirmCheckoutCommand commandFor(final CheckoutSession session) {
    return new ConfirmCheckoutCommand(
        session.id().value(),
        CUSTOMER,
        session.reviewAt(Map.of(THING, Money.euro(10))).fingerprint().value());
  }

  /** One thing at 10 EUR plus 5 EUR shipping, the chosen provider picked, at the review. */
  private CheckoutSession sessionAtTheReview() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(CheckoutLineItemId.generate(), THING, "Thing", Money.euro(10), 1, null);
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(), CustomerId.of(CUSTOMER), List.of(item), Money.euro(10));
    session.submitBuyerInfo(BuyerInfo.of("ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    session.submitDelivery(
        DeliveryAddress.of("123 Main Street", "Springfield", "12345", "United States"),
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", Money.euro(5)));
    session.submitPayment(PaymentSelection.of(CHOSEN));
    session.clearDomainEvents();
    return sessions.save(session);
  }

  /** The article of the session's line, available at 10 EUR. */
  private static final class OneArticle implements CheckoutArticleDataPort {

    @Override
    public Map<ProductId, CheckoutArticle> getArticleData(final Collection<ProductId> productIds) {
      return Map.of(THING, CheckoutArticle.of(THING, "Thing", Money.euro(10), 5, true, null));
    }
  }

  /** A provider that authorizes every payment, under the id it is given. */
  private record AuthorizingProvider(PaymentProviderId providerId) implements PaymentProvider {

    @Override
    public String displayName() {
      return "Authorizing provider";
    }

    @Override
    public String initiatePayment(final CheckoutSessionId sessionId, final Money amount) {
      return "pay-1";
    }

    @Override
    public PaymentResult confirmPayment(final String providerReference) {
      return PaymentResult.success(providerReference);
    }

    @Override
    public PaymentResult cancelPayment(final String providerReference) {
      return PaymentResult.success(providerReference);
    }

    @Override
    public boolean isAvailable() {
      return true;
    }
  }

  /** A registry that answers every id with the one provider it holds. */
  private record AnsweringRegistry(PaymentProvider provider) implements PaymentProviderRegistry {

    @Override
    public Optional<PaymentProvider> findById(final PaymentProviderId providerId) {
      return Optional.of(provider);
    }

    @Override
    public List<PaymentProvider> findAll() {
      return List.of(provider);
    }

    @Override
    public List<PaymentProvider> findAvailable() {
      return List.of(provider);
    }
  }

  /** A stock that reserves every line and remembers what was reserved and given back. */
  private static final class RecordingStockReservation implements StockReservationPort {

    private final List<Line> reserved = new ArrayList<>();
    private final List<Line> released = new ArrayList<>();

    @Override
    public Optional<ProductId> reserve(final List<Line> lines) {
      reserved.addAll(lines);
      return Optional.empty();
    }

    @Override
    public void release(final List<Line> lines) {
      released.addAll(lines);
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
