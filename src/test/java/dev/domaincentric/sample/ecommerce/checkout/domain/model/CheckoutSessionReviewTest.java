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

@DisplayName("CheckoutSession: its review at today's prices, and ordering what the review showed")
class CheckoutSessionReviewTest {

  private static final ProductId DDD = ProductId.generate();
  private static final ProductId IDDD = ProductId.generate();

  @Test
  @DisplayName("the review gives each line today's price and keeps its quantity")
  void theReviewGivesEachLineTodaysPriceAndKeepsItsQuantity() {
    final CheckoutSession session = sessionAtTheReview();

    final CheckoutReview review =
        session.reviewAt(Map.of(DDD, Money.euro(49.99), IDDD, Money.euro(40)));

    assertEquals(
        List.of(Money.euro(49.99), Money.euro(40)),
        review.lineItems().stream().map(CheckoutLineItem::unitPrice).toList());
    assertEquals(
        List.of(1, 2), review.lineItems().stream().map(CheckoutLineItem::quantity).toList());
  }

  @Test
  @DisplayName("a line without today's price keeps its own")
  void aLineWithoutTodaysPriceKeepsItsOwn() {
    final CheckoutSession session = sessionAtTheReview();

    final CheckoutReview review = session.reviewAt(Map.of(DDD, Money.euro(49.99)));

    assertEquals(Money.euro(45), review.lineItems().get(1).unitPrice());
  }

  @Test
  @DisplayName("the review's totals follow today's prices and keep the shipping")
  void theReviewsTotalsFollowTodaysPricesAndKeepTheShipping() {
    final CheckoutSession session = aBookAtTheReview();

    final CheckoutReview review = session.reviewAt(Map.of(DDD, Money.euro(49.99)));

    assertEquals(Money.euro(49.99), review.totals().subtotal());
    assertEquals(Money.euro(4.99), review.totals().shipping());
    assertEquals(Money.euro(54.98), review.totals().total());
  }

  @Test
  @DisplayName("working out the review changes nothing on the session")
  void workingOutTheReviewChangesNothing() {
    final CheckoutSession session = aBookAtTheReview();
    session.clearDomainEvents();
    final List<CheckoutLineItem> lines = List.copyOf(session.lineItems());
    final CheckoutTotals totals = session.totals();

    session.reviewAt(Map.of(DDD, Money.euro(49.99)));

    assertEquals(lines, session.lineItems());
    assertEquals(totals, session.totals());
    assertTrue(session.domainEvents().isEmpty(), "the review raises no event");
  }

  @Test
  @DisplayName("confirming with the shown fingerprint orders the reviewed lines and total")
  void confirmingWithTheShownFingerprintOrdersTheReviewedLinesAndTotal() {
    final CheckoutSession session = aBookAtTheReview();
    final Map<ProductId, Money> today = Map.of(DDD, Money.euro(49.99));
    final ReviewFingerprint shown = session.reviewAt(today).fingerprint();

    session.confirm(CheckoutValidationResult.valid(), today, shown);

    assertEquals(CheckoutSessionStatus.CONFIRMED, session.status());
    assertEquals(Money.euro(49.99), session.lineItems().get(0).unitPrice());
    assertEquals(Money.euro(54.98), session.totals().total());
  }

  @Test
  @DisplayName("confirming with another fingerprint is refused and changes nothing")
  void confirmingWithAnotherFingerprintIsRefusedAndChangesNothing() {
    final CheckoutSession session = aBookAtTheReview();
    final ReviewFingerprint shown = session.reviewAt(Map.of(DDD, Money.euro(54.99))).fingerprint();
    session.clearDomainEvents();
    final CheckoutTotals totals = session.totals();

    assertThrows(
        CheckoutChangedSinceReviewException.class,
        () ->
            session.confirm(
                CheckoutValidationResult.valid(), Map.of(DDD, Money.euro(49.99)), shown));

    assertEquals(CheckoutSessionStatus.ACTIVE, session.status());
    assertEquals(totals, session.totals());
    assertEquals(Money.euro(54.99), session.lineItems().get(0).unitPrice());
    assertTrue(session.domainEvents().isEmpty(), "a refusal raises no event");
  }

  /** "Domain-Driven Design" once at 54.99 with "Standard Shipping" at 4.99, at the review. */
  private static CheckoutSession aBookAtTheReview() {
    return atTheReview(
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(line(DDD, "Domain-Driven Design", Money.euro(54.99), 1)),
            Money.euro(54.99)));
  }

  /** "Domain-Driven Design" once at 54.99 and "Implementing DDD" twice at 45, at the review. */
  private static CheckoutSession sessionAtTheReview() {
    return atTheReview(
        CheckoutSession.start(
            CartId.generate(),
            CustomerId.of("customer-1"),
            List.of(
                line(DDD, "Domain-Driven Design", Money.euro(54.99), 1),
                line(IDDD, "Implementing Domain-Driven Design", Money.euro(45), 2)),
            Money.euro(144.99)));
  }

  private static CheckoutSession atTheReview(final CheckoutSession session) {
    session.submitBuyerInfo(BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    session.submitDelivery(
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"),
        ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99)));
    session.submitPayment(PaymentSelection.of(PaymentProviderId.of("mock")));
    return session;
  }

  private static CheckoutLineItem line(
      final ProductId product, final String name, final Money price, final int quantity) {
    return CheckoutLineItem.of(CheckoutLineItemId.generate(), product, name, price, quantity, null);
  }
}
