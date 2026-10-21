package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewFingerprint: what a review page carries back with Place Order")
class ReviewFingerprintTest {

  @Test
  @DisplayName("refuses a missing value")
  void refusesAMissingValue() {
    assertThrows(IllegalArgumentException.class, () -> ReviewFingerprint.of(null));
  }

  @Test
  @DisplayName("a blank or malformed value matches no review and names no line")
  void aBlankOrMalformedValueMatchesNoReviewAndNamesNoLine() {
    final ReviewFingerprint today = aReview().fingerprint();

    for (final String sent : List.of("", "not-a-fingerprint")) {
      final ReviewFingerprint shown = ReviewFingerprint.of(sent);
      assertNotEquals(today, shown, sent);
      assertEquals(OptionalInt.empty(), today.firstChangedLine(shown), sent);
    }
  }

  private static CheckoutReview aReview() {
    return new CheckoutReview(
        CheckoutSessionId.generate(),
        List.of(
            CheckoutLineItem.of(
                CheckoutLineItemId.generate(),
                ProductId.generate(),
                "Domain-Driven Design",
                Money.euro(54.99),
                1,
                null)),
        CheckoutTotals.calculate(Money.euro(54.99), Money.euro(4.99)),
        BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"),
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"),
        ShippingOption.of("standard", "Standard Shipping", "5-7 business days", Money.euro(4.99)),
        PaymentSelection.of(PaymentProviderId.of("mock")));
  }
}
