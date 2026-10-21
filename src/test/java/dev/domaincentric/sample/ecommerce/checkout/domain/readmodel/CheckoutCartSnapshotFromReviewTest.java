package dev.domaincentric.sample.ecommerce.checkout.domain.readmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutReview;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutTotals;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutCartSnapshot: the session as its review shows it")
class CheckoutCartSnapshotFromReviewTest {

  @Test
  @DisplayName("carries the review's lines and totals, everything else the session's")
  void carriesTheReviewsLinesAndTotalsEverythingElseTheSessions() {
    final CheckoutLineItem captured =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(),
            ProductId.generate(),
            "Domain-Driven Design",
            Money.euro(54.99),
            1,
            null);
    final CheckoutSession session =
        CheckoutSession.start(
            CartId.generate(), CustomerId.of("customer-1"), List.of(captured), Money.euro(54.99));
    session.submitBuyerInfo(BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    session.submitDelivery(
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"),
        ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99)));
    session.submitPayment(PaymentSelection.of(PaymentProviderId.of("mock")));
    final CheckoutTotals todaysTotals =
        CheckoutTotals.calculate(Money.euro(49.99), Money.euro(4.99));
    final CheckoutReview review =
        new CheckoutReview(
            session.id(),
            List.of(captured.withUnitPrice(Money.euro(49.99))),
            todaysTotals,
            session.buyerInfo(),
            session.deliveryAddress(),
            session.shippingOption(),
            session.paymentSelection());

    final CheckoutCartSnapshot snapshot = CheckoutCartSnapshot.from(session, review);

    assertEquals(
        List.of(Money.euro(49.99)),
        snapshot.lineItems().stream().map(LineItemSnapshot::price).toList());
    assertEquals(todaysTotals, snapshot.totals());
    assertEquals(Money.euro(49.99), snapshot.subtotal());
    final CheckoutCartSnapshot asStored = CheckoutCartSnapshot.from(session);
    assertEquals(asStored.sessionId(), snapshot.sessionId());
    assertEquals(asStored.step(), snapshot.step());
    assertEquals(asStored.status(), snapshot.status());
    assertEquals(asStored.buyerInfo(), snapshot.buyerInfo());
    assertEquals(asStored.shippingOption(), snapshot.shippingOption());
    assertEquals(asStored.paymentSelection(), snapshot.paymentSelection());
  }
}
