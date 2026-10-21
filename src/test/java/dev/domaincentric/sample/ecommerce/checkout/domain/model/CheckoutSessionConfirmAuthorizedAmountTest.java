package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: the order is confirmed only at the amount its payment covers")
class CheckoutSessionConfirmAuthorizedAmountTest {

  private static final ProductId DDD = ProductId.generate();
  private static final Map<ProductId, Money> TODAY = Map.of(DDD, Money.euro(49.99));

  @Test
  @DisplayName("a payment authorized for the review's total is confirmed")
  void aPaymentAuthorizedForTheReviewsTotalIsConfirmed() {
    final CheckoutSession session = aBookAtTheReview();
    final ReviewFingerprint shown = session.reviewAt(TODAY).fingerprint();
    session.authorizePayment("pay-1", Money.euro(54.98));

    session.confirm(CheckoutValidationResult.valid(), TODAY, shown);

    assertEquals(CheckoutSessionStatus.CONFIRMED, session.status());
  }

  @Test
  @DisplayName("a payment authorized for another amount is refused and changes nothing")
  void aPaymentAuthorizedForAnotherAmountIsRefusedAndChangesNothing() {
    final CheckoutSession session = aBookAtTheReview();
    final ReviewFingerprint shown = session.reviewAt(TODAY).fingerprint();
    session.authorizePayment("pay-1", Money.euro(59.98));
    session.clearDomainEvents();
    final CheckoutTotals totals = session.totals();

    assertThrows(
        AuthorizedAmountDiffersFromReviewException.class,
        () -> session.confirm(CheckoutValidationResult.valid(), TODAY, shown));

    assertEquals(CheckoutSessionStatus.ACTIVE, session.status());
    assertEquals(totals, session.totals());
    assertTrue(session.domainEvents().isEmpty(), "a refusal raises no event");
  }

  /** "Domain-Driven Design" once at 54.99 with "Standard Shipping" at 4.99, at the review. */
  private static CheckoutSession aBookAtTheReview() {
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(
                CheckoutLineItem.of(
                    CheckoutLineItemId.generate(),
                    DDD,
                    "Domain-Driven Design",
                    Money.euro(54.99),
                    1,
                    null)),
            Money.euro(54.99));
    session.submitBuyerInfo(BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    session.submitDelivery(
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"),
        ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99)));
    session.submitPayment(PaymentSelection.of(PaymentProviderId.of("mock")));
    return session;
  }
}
