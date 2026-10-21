package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutReview: its fingerprint, and refusing a review that is not the one shown")
class CheckoutReviewTest {

  private static final CheckoutSessionId SESSION = CheckoutSessionId.generate();
  private static final CheckoutLineItemId DDD_LINE = CheckoutLineItemId.generate();
  private static final CheckoutLineItemId IDDD_LINE = CheckoutLineItemId.generate();
  private static final ProductId DDD = ProductId.generate();
  private static final ProductId IDDD = ProductId.generate();
  private static final BuyerInfo JANE =
      BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567");
  private static final DeliveryAddress BERLIN =
      DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany");
  private static final ShippingOption STANDARD =
      ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99));
  private static final PaymentSelection MOCK = PaymentSelection.of(PaymentProviderId.of("mock"));

  @Test
  @DisplayName("the same content gives the same fingerprint")
  void theSameContentGivesTheSameFingerprint() {
    assertEquals(
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint(),
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint());
  }

  @Test
  @DisplayName("a different unit price changes the fingerprint")
  void aDifferentUnitPriceChangesTheFingerprint() {
    assertNotEquals(
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint(),
        review(Money.euro(49.99), JANE, BERLIN, STANDARD, MOCK).fingerprint());
  }

  @Test
  @DisplayName(
      "a different buyer, address, shipping method, shipping price or payment method changes it")
  void anyOtherDifferenceChangesTheFingerprint() {
    final ReviewFingerprint shown =
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint();
    final List<CheckoutReview> changed =
        List.of(
            review(
                Money.euro(54.99),
                BuyerInfo.of("john@example.com", "John", "Doe", "+49 30 1234567"),
                BERLIN,
                STANDARD,
                MOCK),
            review(
                Money.euro(54.99),
                JANE,
                DeliveryAddress.of("Hauptstrasse 2", "Berlin", "10115", "Germany"),
                STANDARD,
                MOCK),
            review(
                Money.euro(54.99),
                JANE,
                BERLIN,
                ShippingOption.of(
                    "express", "Express Shipping", "5-7 business days", Money.euro(4.99)),
                MOCK),
            review(
                Money.euro(54.99),
                JANE,
                BERLIN,
                ShippingOption.of(
                    "standard", "Standard Shipping", "5-7 business days", Money.euro(5.99)),
                MOCK),
            review(
                Money.euro(54.99),
                JANE,
                BERLIN,
                STANDARD,
                PaymentSelection.of(PaymentProviderId.of("invoice"))));

    for (final CheckoutReview review : changed) {
      assertNotEquals(shown, review.fingerprint(), review.toString());
    }
  }

  @Test
  @DisplayName("refusing passes for the review's own fingerprint")
  void refusingPassesForTheReviewsOwnFingerprint() {
    final CheckoutReview review = review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK);

    assertDoesNotThrow(() -> review.refuseUnlessShown(review.fingerprint()));
  }

  @Test
  @DisplayName("a line whose price changed is refused, naming that line's product")
  void aLineWhosePriceChangedIsRefusedNamingItsProduct() {
    final ReviewFingerprint shown =
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint();
    final CheckoutReview today = review(Money.euro(49.99), JANE, BERLIN, STANDARD, MOCK);

    final CheckoutChangedSinceReviewException refusal =
        assertThrows(
            CheckoutChangedSinceReviewException.class, () -> today.refuseUnlessShown(shown));

    assertEquals(Optional.of("Domain-Driven Design"), refusal.productWithChangedPrice());
  }

  @Test
  @DisplayName("any other difference is refused without naming a product")
  void anyOtherDifferenceIsRefusedWithoutNamingAProduct() {
    final ReviewFingerprint shown =
        review(Money.euro(54.99), JANE, BERLIN, STANDARD, MOCK).fingerprint();
    final List<CheckoutReview> changed =
        List.of(
            review(
                Money.euro(54.99),
                JANE,
                BERLIN,
                ShippingOption.of(
                    "express", "Express Shipping", "1-2 business days", Money.euro(9.99)),
                MOCK),
            review(
                List.of(
                    implementingDdd(),
                    CheckoutLineItem.of(
                        DDD_LINE, DDD, "Domain-Driven Design", Money.euro(54.99), 2, null))),
            review(
                List.of(
                    implementingDdd(),
                    CheckoutLineItem.of(
                        DDD_LINE,
                        ProductId.generate(),
                        "Domain-Driven Design Distilled",
                        Money.euro(54.99),
                        1,
                        null))));

    for (final CheckoutReview today : changed) {
      final CheckoutChangedSinceReviewException refusal =
          assertThrows(
              CheckoutChangedSinceReviewException.class,
              () -> today.refuseUnlessShown(shown),
              today.toString());

      assertEquals(Optional.empty(), refusal.productWithChangedPrice(), today.toString());
    }
  }

  /**
   * A review of "Implementing Domain-Driven Design" twice at 45 and "Domain-Driven Design" once at
   * the given price, in that order.
   */
  private static CheckoutReview review(
      final Money dddPrice,
      final BuyerInfo buyer,
      final DeliveryAddress address,
      final ShippingOption shipping,
      final PaymentSelection payment) {
    return review(
        List.of(
            implementingDdd(),
            CheckoutLineItem.of(DDD_LINE, DDD, "Domain-Driven Design", dddPrice, 1, null)),
        buyer,
        address,
        shipping,
        payment);
  }

  /** A review of the given lines for Jane, to Berlin, by standard shipping, paid by mock. */
  private static CheckoutReview review(final List<CheckoutLineItem> lines) {
    return review(lines, JANE, BERLIN, STANDARD, MOCK);
  }

  private static CheckoutReview review(
      final List<CheckoutLineItem> lines,
      final BuyerInfo buyer,
      final DeliveryAddress address,
      final ShippingOption shipping,
      final PaymentSelection payment) {
    final Money subtotal =
        lines.stream().map(CheckoutLineItem::lineTotal).reduce(Money.euro(0), Money::add);
    return new CheckoutReview(
        SESSION,
        lines,
        CheckoutTotals.calculate(subtotal, shipping.cost()),
        buyer,
        address,
        shipping,
        payment);
  }

  /** "Implementing Domain-Driven Design" twice at 45. */
  private static CheckoutLineItem implementingDdd() {
    return CheckoutLineItem.of(
        IDDD_LINE, IDDD, "Implementing Domain-Driven Design", Money.euro(45), 2, null);
  }
}
