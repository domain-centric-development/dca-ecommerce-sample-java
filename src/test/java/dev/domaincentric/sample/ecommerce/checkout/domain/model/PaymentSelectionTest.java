package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentSelection: whether an authorized payment stands for a total")
class PaymentSelectionTest {

  private static final PaymentProviderId PROVIDER = PaymentProviderId.of("provider");

  private static Money euro(final String amount) {
    return Money.of(new BigDecimal(amount), Currency.getInstance("EUR"));
  }

  @Test
  @DisplayName("an authorized payment of exactly the total covers it")
  void anAuthorizedPaymentOfTheTotalCoversIt() {
    final PaymentSelection payment = PaymentSelection.authorized(PROVIDER, "pay-1", euro("59.98"));

    assertTrue(payment.covers(PROVIDER, euro("59.98")));
  }

  @Test
  @DisplayName("a payment without a payment reference covers nothing")
  void aPaymentWithoutAReferenceCoversNothing() {
    final PaymentSelection payment = new PaymentSelection(PROVIDER, " ", euro("59.98"));

    assertFalse(payment.covers(PROVIDER, euro("59.98")));
  }

  @Test
  @DisplayName("a payment of another amount does not cover the total")
  void aPaymentOfAnotherAmountDoesNotCoverTheTotal() {
    final PaymentSelection payment = PaymentSelection.authorized(PROVIDER, "pay-1", euro("59.98"));

    assertFalse(payment.covers(PROVIDER, euro("64.98")));
  }

  @Test
  @DisplayName("a payment without an authorized amount covers nothing")
  void aPaymentWithoutAnAuthorizedAmountCoversNothing() {
    final PaymentSelection payment = PaymentSelection.of(PROVIDER, "pay-1");

    assertFalse(payment.covers(PROVIDER, euro("59.98")));
  }

  @Test
  @DisplayName("a payment covers the total only for the provider that authorized it")
  void aPaymentCoversOnlyForItsProvider() {
    final PaymentSelection payment = PaymentSelection.authorized(PROVIDER, "pay-1", euro("59.98"));

    assertFalse(payment.covers(PaymentProviderId.of("mock"), euro("59.98")));
  }
}
