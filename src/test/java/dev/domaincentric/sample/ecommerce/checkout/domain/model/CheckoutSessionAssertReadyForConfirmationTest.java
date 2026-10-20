package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutValidationResult.ValidationError;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: asking whether the order may be confirmed, before its stock is held")
class CheckoutSessionAssertReadyForConfirmationTest {

  private static final ProductId DESK_MODEL = ProductId.generate();

  @Test
  @DisplayName("refuses a session that no longer takes changes")
  void refusesASessionThatNoLongerTakesChanges() {
    final CheckoutSession session = sessionAtTheReview();
    session.supersede();

    assertThrows(
        CheckoutNotModifiableException.class,
        () -> session.assertReadyForConfirmation(CheckoutValidationResult.valid()));
  }

  @Test
  @DisplayName("refuses a session with a step still missing")
  void refusesASessionWithAStepStillMissing() {
    final CheckoutSession session = startedSession();

    assertThrows(
        CheckoutStepNotCompletedException.class,
        () -> session.assertReadyForConfirmation(CheckoutValidationResult.valid()));
  }

  @Test
  @DisplayName("refuses a session that does not stand at the review")
  void refusesASessionThatDoesNotStandAtTheReview() {
    final CheckoutSession session = sessionAtTheReview();
    session.goBackTo(CheckoutStep.PAYMENT);

    assertThrows(
        CheckoutStepOutOfOrderException.class,
        () -> session.assertReadyForConfirmation(CheckoutValidationResult.valid()));
  }

  @Test
  @DisplayName("refuses a verdict that found a line short of stock")
  void refusesAVerdictThatFoundALineShortOfStock() {
    final CheckoutSession session = sessionAtTheReview();
    final CheckoutValidationResult shortOfStock =
        CheckoutValidationResult.withError(
            ValidationError.insufficientStock(DESK_MODEL, "Wooden Hexagon Desk Model", 5, 3));

    final CheckoutValidationException refusal =
        assertThrows(
            CheckoutValidationException.class,
            () -> session.assertReadyForConfirmation(shortOfStock));
    assertEquals(shortOfStock, refusal.validation());
  }

  @Test
  @DisplayName("asking changes nothing: status, step and events stay as they were")
  void askingChangesNothing() {
    final CheckoutSession session = sessionAtTheReview();
    session.clearDomainEvents();

    session.assertReadyForConfirmation(CheckoutValidationResult.valid());

    assertEquals(CheckoutSessionStatus.ACTIVE, session.status());
    assertEquals(CheckoutStep.REVIEW, session.currentStep());
    assertTrue(session.domainEvents().isEmpty(), "asking raises no event");
  }

  /** A session of the desk model five times, nothing entered yet. */
  private static CheckoutSession startedSession() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(),
            DESK_MODEL,
            "Wooden Hexagon Desk Model",
            Money.euro(10),
            5,
            null);
    return CheckoutSession.start(
        CartId.generate(), CustomerId.of("customer-1"), List.of(item), Money.euro(50));
  }

  /** A session with every step entered, standing at the review. */
  private static CheckoutSession sessionAtTheReview() {
    final CheckoutSession session = startedSession();
    session.submitBuyerInfo(BuyerInfo.of("jane@example.com", "Jane", "Doe", "+49 30 1234567"));
    session.submitDelivery(
        DeliveryAddress.of("Hauptstrasse 1", "Berlin", "10115", "Germany"),
        ShippingOption.of("STANDARD", "Standard Shipping", "5-7 days", Money.euro(5)));
    session.submitPayment(PaymentSelection.of(PaymentProviderId.of("mock")));
    return session;
  }
}
