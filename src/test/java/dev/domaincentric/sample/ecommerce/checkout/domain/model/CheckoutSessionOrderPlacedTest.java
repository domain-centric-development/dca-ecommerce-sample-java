package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: asking whether an order was placed with the session")
class CheckoutSessionOrderPlacedTest {

  private static final ProductId DDD = ProductId.generate();
  private static final Map<ProductId, Money> TODAY = Map.of(DDD, Money.euro(49.99));

  @Test
  @DisplayName("an order is placed once the session is confirmed")
  void anOrderIsPlacedOnceTheSessionIsConfirmed() {
    final CheckoutSession session = aConfirmedSession();

    assertTrue(session.isOrderPlaced());
  }

  @Test
  @DisplayName("an order stays placed after the session is completed")
  void anOrderStaysPlacedAfterTheSessionIsCompleted() {
    final CheckoutSession session = aConfirmedSession();

    session.complete("order-1");

    assertTrue(session.isOrderPlaced());
  }

  @Test
  @DisplayName("no order is placed while the session is active")
  void noOrderIsPlacedWhileTheSessionIsActive() {
    final CheckoutSession session = aBookAtTheReview();

    assertFalse(session.isOrderPlaced());
  }

  /** The book at the review, its payment authorized and the order confirmed. */
  private static CheckoutSession aConfirmedSession() {
    final CheckoutSession session = aBookAtTheReview();
    final ReviewFingerprint shown = session.reviewAt(TODAY).fingerprint();
    session.authorizePayment("pay-1", Money.euro(54.98));
    session.confirm(CheckoutValidationResult.valid(), TODAY, shown);
    return session;
  }

  /** "Domain-Driven Design" once with "Standard Shipping" at 4.99, at the review. */
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
