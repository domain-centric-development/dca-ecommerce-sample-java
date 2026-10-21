package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.checkout.domain.event.PaymentSubmitted;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: choosing how to pay, and the payment the provider authorized")
class CheckoutSessionAuthorizePaymentTest {

  private static final PaymentProviderId PROVIDER = PaymentProviderId.of("provider");

  @Test
  @DisplayName("the payment step records the chosen provider without a payment reference")
  void thePaymentStepRecordsTheChosenProviderOnly() {
    final CheckoutSession session = sessionAtThePaymentStep();

    session.submitPayment(PaymentSelection.of(PROVIDER));

    assertEquals(PROVIDER, session.paymentSelection().providerId());
    assertFalse(session.paymentSelection().hasReference());
  }

  @Test
  @DisplayName("the payment step announces no payment")
  void thePaymentStepRaisesNoPaymentSubmitted() {
    final CheckoutSession session = sessionAtThePaymentStep();
    session.clearDomainEvents();

    session.submitPayment(PaymentSelection.of(PROVIDER));

    assertTrue(
        session.domainEvents().stream().noneMatch(PaymentSubmitted.class::isInstance),
        "no payment was requested at the payment step");
  }

  @Test
  @DisplayName("a session that does not stand at the review takes no authorized payment")
  void aSessionNotAtTheReviewTakesNoAuthorizedPayment() {
    final CheckoutSession session = sessionAtTheReview();
    session.goBackTo(CheckoutStep.PAYMENT);

    assertThrows(
        CheckoutStepOutOfOrderException.class,
        () -> session.authorizePayment("pay-1", Money.euro(15)));
  }

  @Test
  @DisplayName("a session that no longer takes changes takes no authorized payment")
  void aSessionThatNoLongerTakesChangesTakesNoAuthorizedPayment() {
    final CheckoutSession session = sessionAtTheReview();
    session.abandon();

    assertThrows(
        CheckoutNotModifiableException.class,
        () -> session.authorizePayment("pay-1", Money.euro(15)));
  }

  @Test
  @DisplayName("the authorized payment carries its reference, the chosen provider and the total")
  void theAuthorizedPaymentIsForTheSessionsTotal() {
    final CheckoutSession session = sessionAtTheReview();

    session.authorizePayment("pay-1", Money.euro(15));

    assertEquals(
        PaymentSelection.authorized(PROVIDER, "pay-1", Money.euro(15)), session.paymentSelection());
  }

  @Test
  @DisplayName("the authorized payment records the amount the provider authorized")
  void theAuthorizedPaymentIsForTheAmountAuthorized() {
    final CheckoutSession session = sessionAtTheReview();

    // The provider authorized 14 EUR — what the review showed — while the session holds 15 EUR
    session.authorizePayment("pay-1", Money.euro(14));

    assertEquals(
        PaymentSelection.authorized(PROVIDER, "pay-1", Money.euro(14)), session.paymentSelection());
  }

  @Test
  @DisplayName("the authorized payment is announced")
  void theAuthorizedPaymentRaisesPaymentSubmitted() {
    final CheckoutSession session = sessionAtTheReview();
    session.clearDomainEvents();

    session.authorizePayment("pay-1", Money.euro(15));

    final List<DomainEvent> events = session.domainEvents();
    assertEquals(1, events.size());
    assertEquals(session.id(), ((PaymentSubmitted) events.getFirst()).sessionId());
  }

  /** A session of 10 EUR plus 5 EUR shipping whose buyer and delivery step are done. */
  private static CheckoutSession sessionAtThePaymentStep() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(), ProductId.generate(), "Thing", Money.euro(10), 1, null);
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(), CustomerId.of("customer-1"), List.of(item), Money.euro(10));
    session.submitBuyerInfo(BuyerInfo.of("ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    session.submitDelivery(
        DeliveryAddress.of("123 Main Street", "Springfield", "12345", "United States"),
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", Money.euro(5)));
    return session;
  }

  /** That session with the provider chosen, standing at the review. */
  private static CheckoutSession sessionAtTheReview() {
    final CheckoutSession session = sessionAtThePaymentStep();
    session.submitPayment(PaymentSelection.of(PROVIDER));
    return session;
  }
}
