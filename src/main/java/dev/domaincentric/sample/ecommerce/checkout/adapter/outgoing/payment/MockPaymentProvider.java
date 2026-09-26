package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.payment;

import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProvider;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Mock implementation of PaymentProvider for testing and development.
 *
 * <p>This adapter simulates payment processing without connecting to any real payment gateway. All
 * payments are automatically approved, making it suitable for local development, testing, and
 * demonstration purposes.
 *
 * <p>The provider generates mock transaction references in the format "mock-{uuid}" to simulate
 * real provider behavior.
 *
 * <p>It stands in only where no provider address is configured ({@code
 * checkout.payment-provider.base-url}); with an address, {@link RestPaymentProvider} takes its
 * place, so running the shop locally needs no provider.
 */
@Component
@ConditionalOnExpression("'${checkout.payment-provider.base-url:}'.isBlank()")
public class MockPaymentProvider implements PaymentProvider {

  public static final PaymentProviderId PROVIDER_ID = PaymentProviderId.of("mock");
  private static final String DISPLAY_NAME = "Mock Payment (Test)";

  private boolean available = true;

  @Override
  public PaymentProviderId providerId() {
    return PROVIDER_ID;
  }

  @Override
  public String displayName() {
    return DISPLAY_NAME;
  }

  @Override
  public PaymentResult initiatePayment(final CheckoutSessionId sessionId, final Money amount) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    // Generate a mock payment reference
    final String reference = "mock-" + UUID.randomUUID().toString();
    return PaymentResult.success(reference);
  }

  @Override
  public PaymentResult confirmPayment(final String providerReference) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    if (!providerReference.startsWith("mock-")) {
      return PaymentResult.failure("Invalid mock payment reference: " + providerReference);
    }

    // Mock payments are always confirmed successfully
    return PaymentResult.success(providerReference);
  }

  @Override
  public PaymentResult cancelPayment(final String providerReference) {
    if (!available) {
      return PaymentResult.failure("Mock payment provider is currently unavailable");
    }

    if (!providerReference.startsWith("mock-")) {
      return PaymentResult.failure("Invalid mock payment reference: " + providerReference);
    }

    // Mock payments are always cancelled successfully
    return PaymentResult.success(providerReference);
  }

  @Override
  public boolean isAvailable() {
    return available;
  }

  /**
   * Sets the availability state of this mock provider.
   *
   * <p>This method is useful for testing error scenarios where the payment provider becomes
   * temporarily unavailable.
   *
   * @param available true to make the provider available, false to simulate unavailability
   */
  public void setAvailable(final boolean available) {
    this.available = available;
  }
}
