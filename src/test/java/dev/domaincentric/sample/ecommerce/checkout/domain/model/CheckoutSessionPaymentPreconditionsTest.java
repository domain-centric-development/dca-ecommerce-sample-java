package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: what choosing how to pay and recording the authorized payment need")
class CheckoutSessionPaymentPreconditionsTest {

  private static final PaymentProviderId PROVIDER = PaymentProviderId.of("provider");

  @Test
  @DisplayName("a session with nothing to pay takes no way to pay")
  void aSessionWithNothingToPayTakesNoWayToPay() {
    final CheckoutSession session = sessionAtThePaymentStep(Money.euro(0), Money.euro(0));

    assertThrows(
        NothingToPayException.class, () -> session.submitPayment(PaymentSelection.of(PROVIDER)));
    assertNull(session.paymentSelection());
  }

  @Test
  @DisplayName("a session with a step still missing takes no authorized payment")
  void aSessionWithAStepMissingTakesNoAuthorizedPayment() {
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(line(Money.euro(10))),
            Money.euro(10));

    assertThrows(
        CheckoutStepNotCompletedException.class,
        () ->
            session.authorizePayment(
                PaymentSelection.authorized(PROVIDER, "pay-1", Money.euro(10))));
  }

  private static CheckoutLineItem line(final Money unitPrice) {
    return CheckoutLineItem.of(
        CheckoutLineItemId.generate(), ProductId.generate(), "Thing", unitPrice, 1, null);
  }

  /** A session whose buyer and delivery step are done, at that item price and shipping cost. */
  private static CheckoutSession sessionAtThePaymentStep(
      final Money unitPrice, final Money shipping) {
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(), CustomerId.of("customer-1"), List.of(line(unitPrice)), unitPrice);
    session.submitBuyerInfo(BuyerInfo.of("ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    session.submitDelivery(
        DeliveryAddress.of("123 Main Street", "Springfield", "12345", "United States"),
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", shipping));
    return session;
  }
}
