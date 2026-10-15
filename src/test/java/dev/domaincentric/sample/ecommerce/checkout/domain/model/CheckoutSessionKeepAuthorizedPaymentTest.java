package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: keeping the payment the provider already authorized")
class CheckoutSessionKeepAuthorizedPaymentTest {

  private static final PaymentProviderId PROVIDER = PaymentProviderId.of("provider");

  @Test
  @DisplayName("refuses to keep a payment that does not cover the total")
  void refusesToKeepAPaymentThatDoesNotCoverTheTotal() {
    final CheckoutSession session = sessionPaidFor(Money.euro(5));
    session.goBackTo(CheckoutStep.PAYMENT);

    assertThrows(
        CheckoutStepNotCompletedException.class, () -> session.keepAuthorizedPayment(PROVIDER));
    assertEquals(CheckoutStep.PAYMENT, session.currentStep());
  }

  @Test
  @DisplayName("keeping the payment moves the session from payment to review")
  void keepingThePaymentMovesTheSessionToReview() {
    final CheckoutSession session = sessionPaidFor(Money.euro(15));
    session.goBackTo(CheckoutStep.PAYMENT);

    session.keepAuthorizedPayment(PROVIDER);

    assertEquals(CheckoutStep.REVIEW, session.currentStep());
  }

  @Test
  @DisplayName("keeping the payment raises no second PaymentSubmitted")
  void keepingThePaymentRaisesNoSecondPaymentSubmitted() {
    final CheckoutSession session = sessionPaidFor(Money.euro(15));
    session.goBackTo(CheckoutStep.PAYMENT);
    session.clearDomainEvents();

    session.keepAuthorizedPayment(PROVIDER);

    assertTrue(
        session.domainEvents().isEmpty(), "the payment was announced when it was authorized");
  }

  @Test
  @DisplayName("a released payment covers nothing")
  void aReleasedPaymentCoversNothing() {
    final CheckoutSession session = sessionPaidFor(Money.euro(15));
    session.goBackTo(CheckoutStep.PAYMENT);

    session.releasePayment("pay-1");

    assertFalse(session.authorizedPaymentCovers(PROVIDER));
  }

  /**
   * A session of 10 EUR plus 5 EUR shipping whose payment the provider authorized for {@code
   * amount}.
   */
  private static CheckoutSession sessionPaidFor(final Money amount) {
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
    session.submitPayment(PaymentSelection.authorized(PROVIDER, "pay-1", amount));
    return session;
  }
}
