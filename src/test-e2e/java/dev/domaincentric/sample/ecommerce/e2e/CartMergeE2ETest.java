package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.e2e.pages.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E test for the cart merge flow.
 *
 * <p>Tests the full flow of:
 *
 * <ol>
 *   <li>User adds items to cart as guest
 *   <li>User registers/logs in with existing cart having items
 *   <li>User sees merge options page
 *   <li>User selects merge option
 *   <li>Cart shows combined items
 * </ol>
 *
 * <p>Prerequisites:
 *
 * <ul>
 *   <li>Application running at configured base URL
 *   <li>Sample products loaded in database
 * </ul>
 */
@DisplayName("Cart Merge E2E Tests")
class CartMergeE2ETest extends BaseE2ETest {

  private static final String TEST_PASSWORD = "SecurePassword123!";

  @Test
  @DisplayName(
      "Full flow: login with both carts, see merge options, select merge, verify combined cart")
  void fullCartMergeFlow() {
    // Step 1: Register a new user and add a product to their account cart
    String uniqueEmail = "merge-test-" + System.currentTimeMillis() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Add first product to account cart while logged in
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCartAndStay();

    // Verify cart has the item
    CartPage cart = CartPage.navigateTo(page);
    int accountCartItems = cart.waitForItems().getItemCount();
    assertTrue(accountCartItems > 0, "Account cart should have items");

    // Step 2: Logout (clear cookies) to become anonymous
    context.clearCookies();

    // Step 3: Add a different product to cart as anonymous user
    catalog = ProductCatalogPage.navigateTo(page);
    // Navigate to a product (first one is fine - system should handle same product)
    detail = catalog.viewFirstProduct();
    detail.addToCartAndStay();

    // Verify anonymous cart has the item
    cart = CartPage.navigateTo(page);
    cart.waitForItems();
    int anonymousCartItems = cart.getItemCount();
    assertTrue(anonymousCartItems > 0, "Anonymous cart should have items");

    // Step 4: Login with the registered account - should redirect to merge options
    LoginPage login = LoginPage.navigateTo(page);
    login.login(uniqueEmail, TEST_PASSWORD);

    // Wait for redirect after login to complete
    page.waitForURL(url -> !url.contains("/login"));

    // Step 5: Logging in with items in both carts leaves a decision to make — the merge page
    assertTrue(
        getCurrentPath().contains("/cart/merge"),
        "Should be on the merge page. Got: " + getCurrentPath());
    CartMergePage mergePage = new CartMergePage(page, true);
    assertTrue(mergePage.showsAnonymousCart(), "Should show anonymous cart summary");
    assertTrue(mergePage.showsAccountCart(), "Should show account cart summary");
    assertTrue(mergePage.showsMergeOptions(), "Should show all merge options");

    // Step 6: Select "Merge Both Carts" option and submit
    cart = mergePage.mergeBothCarts();

    // Step 7: Verify the merged cart
    cart.waitForItems();
    assertTrue(cart.hasItems(), "Merged cart should have items");
    assertTrue(getCurrentPath().startsWith("/cart"), "Should be on cart page after merge");
  }
}
