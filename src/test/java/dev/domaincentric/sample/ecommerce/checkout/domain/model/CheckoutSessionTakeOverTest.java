package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CheckoutSession: the customer whose cart it was started from takes it over")
class CheckoutSessionTakeOverTest {

  private static final CustomerId GUEST = CustomerId.of("guest-1");
  private static final CustomerId ACCOUNT = CustomerId.of("account-1");

  @Test
  @DisplayName("keeps the session's id, cart, step and line items")
  void keepsTheSessionsIdCartStepAndLineItems() {
    final CheckoutSession session = aGuestSessionOnTheDeliveryStep();
    final CheckoutSessionId id = session.id();
    final CartId cart = session.cartId();
    final List<CheckoutLineItem> lineItems = List.copyOf(session.lineItems());

    session.takeOverBy(ACCOUNT);

    assertEquals(id, session.id());
    assertEquals(cart, session.cartId());
    assertEquals(CheckoutStep.DELIVERY, session.currentStep());
    assertEquals(lineItems, session.lineItems());
  }

  @Test
  @DisplayName("the session belongs to the customer afterwards")
  void theSessionBelongsToTheCustomerAfterwards() {
    final CheckoutSession session = aGuestSessionOnTheDeliveryStep();

    session.takeOverBy(ACCOUNT);

    assertEquals(ACCOUNT, session.customerId());
  }

  @Test
  @DisplayName("refuses a session that is no longer active")
  void refusesASessionThatIsNoLongerActive() {
    final CheckoutSession session = aGuestSessionOnTheDeliveryStep();
    session.abandon();

    assertThrows(CheckoutNotModifiableException.class, () -> session.takeOverBy(ACCOUNT));
    assertEquals(GUEST, session.customerId());
  }

  @Test
  @DisplayName("raises no event")
  void raisesNoEvent() {
    final CheckoutSession session = aGuestSessionOnTheDeliveryStep();
    session.clearDomainEvents();

    session.takeOverBy(ACCOUNT);

    assertTrue(session.domainEvents().isEmpty(), "nothing the session holds changed");
  }

  /** A guest's active session of one line item, with the buyer step submitted. */
  private static CheckoutSession aGuestSessionOnTheDeliveryStep() {
    final CheckoutLineItem item =
        CheckoutLineItem.of(
            CheckoutLineItemId.generate(), ProductId.generate(), "Thing", Money.euro(10), 1, null);
    final CheckoutSession session =
        CheckoutSession.start(CartId.generate(), GUEST, List.of(item), Money.euro(10));
    session.submitBuyerInfo(BuyerInfo.of("ada@example.com", "Ada", "Lovelace", "+1-555-0100"));
    return session;
  }
}
