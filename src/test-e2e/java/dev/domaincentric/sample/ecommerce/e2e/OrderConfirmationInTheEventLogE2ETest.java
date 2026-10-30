package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ConfirmationPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest places an order; the operator finds the delivery of the confirmed checkout to the order
 * confirmation in the event log — a reaction to the checkout's own event, listed like any delivery
 * the shop makes after a commit.
 */
@DisplayName("The event log lists a reaction to a context's own event")
class OrderConfirmationInTheEventLogE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  private static final String BUYER_EMAIL = "jane@example.com";

  /** The text the entry of the order confirmation's reaction shows under "Listener". */
  private static final String ORDER_CONFIRMATION_LISTENER = "OrderConfirmation";

  /** The type label of the checkout's own event, the confirmed checkout. */
  private static final String CONFIRMED_CHECKOUT = "CheckoutConfirmed";

  /** How long the delivery after the order's commit may take to complete. */
  private static final Duration DELIVERY_TIME = Duration.ofSeconds(15);

  @Test
  @DisplayName("The log lists the order confirmations delivery")
  void theLogListsTheDeliveryOfTheOrderConfirmation() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    final ReviewPage review =
        CartPage.navigateTo(page).proceedToTheReviewWithBuyerEmail(BUYER_EMAIL);
    final ConfirmationPage confirmation = review.placeOrder();
    final String orderReference = confirmation.orderReference();
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();

    final BackofficeEventsPage events =
        BackofficeEventsPage.navigateTo(page)
            .reloadUntilEntryCompleted(orderReference, ORDER_CONFIRMATION_LISTENER, DELIVERY_TIME);

    assertEquals(
        CONFIRMED_CHECKOUT,
        events.typeOfEntry(orderReference, ORDER_CONFIRMATION_LISTENER),
        "the type label of the order confirmation's entry");
    assertEquals(
        "Completed",
        events.statusOfEntry(orderReference, ORDER_CONFIRMATION_LISTENER),
        "the entry's badge");
  }
}
