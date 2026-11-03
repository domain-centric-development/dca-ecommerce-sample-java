package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: the payment is authorized by the provider the review showed")
class CheckoutSessionPaymentProviderTest {

  private static final PaymentProviderId CHOSEN = PaymentProviderId.of("chosen");
  private static final PaymentProviderId OTHER = PaymentProviderId.of("other");

  @Test
  @DisplayName("a payment the chosen provider authorized is recorded")
  void aPaymentTheChosenProviderAuthorizedIsRecorded() {
    final CheckoutSession session = sessionAtTheReview();
    final PaymentSelection authorized =
        PaymentSelection.authorized(CHOSEN, "pay-1", Money.euro(15));

    session.authorizePayment(authorized);

    assertEquals(authorized, session.paymentSelection());
  }

  @Test
  @DisplayName("a payment another provider authorized is refused and changes nothing")
  void aPaymentAnotherProviderAuthorizedIsRefusedAndChangesNothing() {
    final CheckoutSession session = sessionAtTheReview();
    session.clearDomainEvents();

    assertThrows(
        PaymentDiffersFromReviewException.class,
        () ->
            session.authorizePayment(PaymentSelection.authorized(OTHER, "pay-1", Money.euro(15))));

    assertEquals(PaymentSelection.of(CHOSEN), session.paymentSelection());
    assertEquals(CheckoutSessionStatus.ACTIVE, session.status());
    assertTrue(session.domainEvents().isEmpty(), "a refusal raises no event");
  }

  @Test
  @DisplayName(
      "a payment without a reference or an authorized amount is refused and changes nothing")
  void aPaymentWithoutReferenceOrAmountIsRefusedAndChangesNothing() {
    final CheckoutSession session = sessionAtTheReview();
    session.clearDomainEvents();

    assertThrows(
        IllegalArgumentException.class,
        () -> session.authorizePayment(PaymentSelection.of(CHOSEN)));
    assertThrows(
        IllegalArgumentException.class,
        () -> session.authorizePayment(PaymentSelection.of(CHOSEN, "pay-1")));

    assertEquals(PaymentSelection.of(CHOSEN), session.paymentSelection());
    assertTrue(session.domainEvents().isEmpty(), "a refusal raises no event");
  }

  /** A session of 10 EUR plus 5 EUR shipping with the provider chosen, standing at the review. */
  private static CheckoutSession sessionAtTheReview() {
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
    session.submitPayment(PaymentSelection.of(CHOSEN));
    return session;
  }
}
