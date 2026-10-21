package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentSelection: the way to pay a customer chose")
class PaymentSelectionTest {

  @Test
  @DisplayName("a payment selection needs a provider")
  void aPaymentSelectionNeedsAProvider() {
    assertThrows(IllegalArgumentException.class, () -> new PaymentSelection(null, null, null));
  }
}
