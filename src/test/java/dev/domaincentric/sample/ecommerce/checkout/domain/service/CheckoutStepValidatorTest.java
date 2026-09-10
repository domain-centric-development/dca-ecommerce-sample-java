package dev.domaincentric.sample.ecommerce.checkout.domain.service;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.BuyerInfo;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItem;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutLineItemId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.DeliveryAddress;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentProviderId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.PaymentSelection;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.ShippingOption;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.checkout.domain.readmodel.CheckoutCartSnapshot;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CheckoutStepValidatorTest {

  private static final Currency EUR = Currency.getInstance("EUR");
  private CheckoutStepValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CheckoutStepValidator();
  }

  @Nested
  @DisplayName("Invalid Session Tests")
  class InvalidSessionTests {

    @Test
    @DisplayName("null session redirects to cart")
    void nullSessionRedirectsToCart() {
      StepAccess access = validator.accessTo(null, CheckoutStep.BUYER_INFO);

      assertTrue(access.isBackToCart());
    }

    @Test
    @DisplayName("null session redirects to cart for any step")
    void nullSessionRedirectsToCartForAnyStep() {
      for (CheckoutStep step : CheckoutStep.values()) {
        StepAccess access = validator.accessTo(null, step);
        assertTrue(access.isBackToCart(), "Should redirect for step: " + step);
      }
    }
  }

  @Nested
  @DisplayName("Skip Ahead Prevention Tests")
  class SkipAheadTests {

    @Test
    @DisplayName("cannot skip to DELIVERY from BUYER_INFO")
    void cannotSkipToDeliveryFromBuyerInfo() {
      var session = createActiveSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.DELIVERY);

      assertEquals(StepAccess.redirectTo(CheckoutStep.BUYER_INFO), access);
    }

    @Test
    @DisplayName("cannot skip to PAYMENT from BUYER_INFO")
    void cannotSkipToPaymentFromBuyerInfo() {
      var session = createActiveSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.PAYMENT);

      assertEquals(StepAccess.redirectTo(CheckoutStep.BUYER_INFO), access);
    }

    @Test
    @DisplayName("cannot skip to REVIEW from BUYER_INFO")
    void cannotSkipToReviewFromBuyerInfo() {
      var session = createActiveSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.REVIEW);

      assertEquals(StepAccess.redirectTo(CheckoutStep.BUYER_INFO), access);
    }

    @Test
    @DisplayName("cannot skip to CONFIRMATION from BUYER_INFO")
    void cannotSkipToConfirmationFromBuyerInfo() {
      var session = createActiveSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.CONFIRMATION);

      assertEquals(StepAccess.redirectTo(CheckoutStep.BUYER_INFO), access);
    }

    @Test
    @DisplayName("cannot skip to PAYMENT from DELIVERY")
    void cannotSkipToPaymentFromDelivery() {
      var session = createSessionAtDelivery();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.PAYMENT);

      assertEquals(StepAccess.redirectTo(CheckoutStep.DELIVERY), access);
    }

    @Test
    @DisplayName("cannot skip to REVIEW from PAYMENT")
    void cannotSkipToReviewFromPayment() {
      var session = createSessionAtPayment();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.REVIEW);

      assertEquals(StepAccess.redirectTo(CheckoutStep.PAYMENT), access);
    }
  }

  @Nested
  @DisplayName("Go Back Tests")
  class GoBackTests {

    @Test
    @DisplayName("can go back to BUYER_INFO from DELIVERY")
    void canGoBackToBuyerInfoFromDelivery() {
      var session = createSessionAtDelivery();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.BUYER_INFO);

      assertTrue(access.granted(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to BUYER_INFO from PAYMENT")
    void canGoBackToBuyerInfoFromPayment() {
      var session = createSessionAtPayment();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.BUYER_INFO);

      assertTrue(access.granted(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to DELIVERY from PAYMENT")
    void canGoBackToDeliveryFromPayment() {
      var session = createSessionAtPayment();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.DELIVERY);

      assertTrue(access.granted(), "Should allow going back to DELIVERY");
    }

    @Test
    @DisplayName("can go back to BUYER_INFO from REVIEW")
    void canGoBackToBuyerInfoFromReview() {
      var session = createSessionAtReview();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.BUYER_INFO);

      assertTrue(access.granted(), "Should allow going back to BUYER_INFO");
    }

    @Test
    @DisplayName("can go back to DELIVERY from REVIEW")
    void canGoBackToDeliveryFromReview() {
      var session = createSessionAtReview();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.DELIVERY);

      assertTrue(access.granted(), "Should allow going back to DELIVERY");
    }

    @Test
    @DisplayName("can go back to PAYMENT from REVIEW")
    void canGoBackToPaymentFromReview() {
      var session = createSessionAtReview();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.PAYMENT);

      assertTrue(access.granted(), "Should allow going back to PAYMENT");
    }
  }

  @Nested
  @DisplayName("Terminal State Tests")
  class TerminalStateTests {

    @Test
    @DisplayName("completed session allows CONFIRMATION access")
    void completedSessionAllowsConfirmationAccess() {
      var session = createCompletedSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.CONFIRMATION);

      assertTrue(access.granted(), "Should allow CONFIRMATION access for completed session");
    }

    @Test
    @DisplayName("completed session redirects other steps to CONFIRMATION")
    void completedSessionRedirectsOtherStepsToConfirmation() {
      var session = createCompletedSession();

      for (CheckoutStep step :
          List.of(
              CheckoutStep.BUYER_INFO, CheckoutStep.DELIVERY,
              CheckoutStep.PAYMENT, CheckoutStep.REVIEW)) {
        StepAccess access = validator.accessTo(snapshot(session), step);
        assertEquals(
            StepAccess.redirectTo(CheckoutStep.CONFIRMATION),
            access,
            "Should redirect for step: " + step);
      }
    }

    @Test
    @DisplayName("abandoned session redirects to cart")
    void abandonedSessionRedirectsToCart() {
      var session = createAbandonedSession();

      for (CheckoutStep step : CheckoutStep.values()) {
        StepAccess access = validator.accessTo(snapshot(session), step);
        assertTrue(access.isBackToCart(), "Should redirect for step: " + step);
      }
    }

    @Test
    @DisplayName("expired session redirects to cart")
    void expiredSessionRedirectsToCart() {
      var session = createExpiredSession();

      for (CheckoutStep step : CheckoutStep.values()) {
        StepAccess access = validator.accessTo(snapshot(session), step);
        assertTrue(access.isBackToCart(), "Should redirect for step: " + step);
      }
    }

    @Test
    @DisplayName("confirmed session allows CONFIRMATION access")
    void confirmedSessionAllowsConfirmationAccess() {
      var session = createConfirmedSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.CONFIRMATION);

      assertTrue(access.granted(), "Should allow CONFIRMATION access for confirmed session");
    }

    @Test
    @DisplayName("confirmed session redirects other steps to CONFIRMATION")
    void confirmedSessionRedirectsOtherStepsToConfirmation() {
      var session = createConfirmedSession();

      for (CheckoutStep step :
          List.of(
              CheckoutStep.BUYER_INFO, CheckoutStep.DELIVERY,
              CheckoutStep.PAYMENT, CheckoutStep.REVIEW)) {
        StepAccess access = validator.accessTo(snapshot(session), step);
        assertEquals(
            StepAccess.redirectTo(CheckoutStep.CONFIRMATION),
            access,
            "Should redirect for step: " + step);
      }
    }
  }

  @Nested
  @DisplayName("Valid Access Tests")
  class ValidAccessTests {

    @Test
    @DisplayName("allows access to current step")
    void allowsAccessToCurrentStep() {
      var session = createActiveSession();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.BUYER_INFO);

      assertTrue(access.granted(), "Should allow access to current step");
    }

    @Test
    @DisplayName("allows access to DELIVERY when at DELIVERY step")
    void allowsAccessToDeliveryWhenAtDeliveryStep() {
      var session = createSessionAtDelivery();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.DELIVERY);

      assertTrue(access.granted(), "Should allow access to DELIVERY step");
    }

    @Test
    @DisplayName("allows access to PAYMENT when at PAYMENT step")
    void allowsAccessToPaymentWhenAtPaymentStep() {
      var session = createSessionAtPayment();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.PAYMENT);

      assertTrue(access.granted(), "Should allow access to PAYMENT step");
    }

    @Test
    @DisplayName("allows access to REVIEW when at REVIEW step")
    void allowsAccessToReviewWhenAtReviewStep() {
      var session = createSessionAtReview();

      StepAccess access = validator.accessTo(snapshot(session), CheckoutStep.REVIEW);

      assertTrue(access.granted(), "Should allow access to REVIEW step");
    }
  }

  // Helper methods to create test sessions in various states

  /** The validator decides on the read model the web adapters hold, not on the aggregate. */
  private static CheckoutCartSnapshot snapshot(final CheckoutSession session) {
    return CheckoutCartSnapshot.from(session);
  }

  private CheckoutSession createActiveSession() {
    return CheckoutSession.start(
        CartId.generate(),
        CustomerId.of(UUID.randomUUID().toString()),
        List.of(createLineItem()),
        Money.of(BigDecimal.valueOf(100), EUR),
        new TaxCalculator());
  }

  private CheckoutSession createSessionAtDelivery() {
    CheckoutSession session = createActiveSession();
    session.submitBuyerInfo(createBuyerInfo());
    return session;
  }

  private CheckoutSession createSessionAtPayment() {
    CheckoutSession session = createSessionAtDelivery();
    session.submitDelivery(createDeliveryAddress(), createShippingOption(), new TaxCalculator());
    return session;
  }

  private CheckoutSession createSessionAtReview() {
    CheckoutSession session = createSessionAtPayment();
    session.submitPayment(createPaymentSelection());
    return session;
  }

  private CheckoutSession createConfirmedSession() {
    CheckoutSession session = createSessionAtReview();
    session.confirm(
        session.lineItems().stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    item -> item.productId(),
                    item ->
                        new dev.domaincentric.sample.ecommerce.checkout.domain.model
                            .CheckoutArticlePriceResolver.ArticlePrice(
                            item.unitPrice(), true, item.quantity()))));
    return session;
  }

  private CheckoutSession createCompletedSession() {
    CheckoutSession session = createConfirmedSession();
    session.complete("ORD-123");
    return session;
  }

  private CheckoutSession createAbandonedSession() {
    CheckoutSession session = createActiveSession();
    session.abandon();
    return session;
  }

  private CheckoutSession createExpiredSession() {
    CheckoutSession session = createActiveSession();
    session.expire();
    return session;
  }

  private CheckoutLineItem createLineItem() {
    return CheckoutLineItem.of(
        CheckoutLineItemId.generate(),
        ProductId.generate(),
        "Test Product",
        Money.of(BigDecimal.valueOf(100), EUR),
        1,
        null);
  }

  private BuyerInfo createBuyerInfo() {
    return BuyerInfo.of("test@example.com", "John", "Doe", "+1234567890");
  }

  private DeliveryAddress createDeliveryAddress() {
    return DeliveryAddress.of("123 Main St", "Anytown", "12345", "Germany");
  }

  private ShippingOption createShippingOption() {
    return ShippingOption.of(
        "standard", "Standard Shipping", "3-5 business days", Money.of(BigDecimal.valueOf(5), EUR));
  }

  private PaymentSelection createPaymentSelection() {
    return PaymentSelection.of(PaymentProviderId.of("stripe"));
  }
}
