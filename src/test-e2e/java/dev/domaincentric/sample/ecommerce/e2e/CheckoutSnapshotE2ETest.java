package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.e2e.pages.*;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the checkout snapshot policy: a checkout session is a copy of the submitted cart
 * positions, cart edits leave it untouched, a second checkout supersedes the open session, and a
 * completed checkout reconciles only the purchased snapshot — later additions survive.
 *
 * <p>Same scenario, page objects and {@code data-test} selectors as the .NET sample's {@code
 * CheckoutSnapshotE2eTest}; either suite can be pointed at either shop.
 */
@DisplayName("Checkout Snapshot E2E Tests")
class CheckoutSnapshotE2ETest extends BaseE2ETest {

  private static final int PRODUCT_A = 0;
  private static final int PRODUCT_B = 1;
  private static final int PRODUCT_C = 2;

  @Test
  @DisplayName("Cart edits do not change the open checkout session; a new checkout supersedes it")
  void cartEditsLeaveTheSnapshotUntouchedAndRestartReplacesIt() {
    addToCart(PRODUCT_A, 2);
    String productA = CartPage.navigateTo(page).lineItems().get(0);
    assertTrue(productA.endsWith(" x2"), "Cart should hold product A twice, was: " + productA);
    String nameA = productA.substring(0, productA.lastIndexOf(" x"));

    BuyerInfoPage firstSession = CartPage.navigateTo(page).proceedToCheckout();
    assertEquals(List.of(nameA + " x2"), firstSession.summaryItems(), "Session snapshot is A x2");

    // Edit the cart while the session is open: more of A, plus product B
    addToCart(PRODUCT_A, 3);
    addToCart(PRODUCT_B, 1);
    List<String> cartLines = CartPage.navigateTo(page).lineItems();
    assertEquals(2, cartLines.size(), "Cart has two lines after the edits: " + cartLines);
    assertTrue(cartLines.get(0).endsWith(" x5"), "Cart holds A five times: " + cartLines);

    navigateTo("/checkout/buyer");
    BuyerInfoPage stillFirstSession = new BuyerInfoPage(page);
    assertEquals(
        List.of(nameA + " x2"),
        stillFirstSession.summaryItems(),
        "The open session still shows the snapshot it was started from");

    // A second checkout creates a fresh snapshot and supersedes the first session
    BuyerInfoPage secondSession = CartPage.navigateTo(page).proceedToCheckout();
    List<String> summary = secondSession.summaryItems();
    assertEquals(2, summary.size(), "New session snapshots both cart lines: " + summary);
    assertEquals(nameA + " x5", summary.get(0), "New snapshot carries the current quantity of A");
  }

  @Test
  @DisplayName("Completing a checkout removes only the purchased snapshot from the cart")
  void completionReconcilesOnlyThePurchasedSnapshot() {
    addToCart(PRODUCT_A, 2);
    addToCart(PRODUCT_B, 1);
    BuyerInfoPage buyer = CartPage.navigateTo(page).proceedToCheckout();
    assertEquals(2, buyer.summaryItems().size(), "Snapshot holds A and B");

    // A later addition that is not part of the snapshot
    addToCart(PRODUCT_C, 1);
    List<String> before = CartPage.navigateTo(page).lineItems();
    assertEquals(3, before.size(), "Cart holds A, B and C before completion: " + before);
    String lineC = before.get(2);

    navigateTo("/checkout/buyer");
    DeliveryPage delivery =
        new BuyerInfoPage(page)
            .fillBuyerInfo("snapshot@example.com", "Snap", "Shot", "+1-555-0100")
            .continueToDelivery();
    PaymentPage payment =
        delivery
            .fillAddress("123 Main Street", "Springfield", "12345", "United States", "IL")
            .selectFirstShippingOption()
            .continueToPayment();
    ConfirmationPage confirmation =
        payment.selectFirstPaymentProvider().continueToReview().placeOrder();
    assertTrue(confirmation.isOrderConfirmed(), "Order should be confirmed");

    // Reconciliation is asynchronous (integration event); the later addition must survive
    CartPage cart = CartPage.navigateTo(page);
    page.waitForCondition(
        () -> CartPage.navigateTo(page).getItemCount() == 1,
        new com.microsoft.playwright.Page.WaitForConditionOptions().setTimeout(15_000));
    assertEquals(
        List.of(lineC), cart.lineItems(), "Only the later-added product C remains in the cart");

    // The cart stays editable after the purchase
    addToCart(PRODUCT_B, 1);
    assertEquals(2, CartPage.navigateTo(page).getItemCount(), "Cart accepts new items again");
  }

  private void addToCart(int productIndex, int times) {
    for (int i = 0; i < times; i++) {
      ProductCatalogPage.navigateTo(page).viewProduct(productIndex).addToCart();
    }
  }
}
