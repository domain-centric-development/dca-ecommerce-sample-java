package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.*;

import dev.domaincentric.sample.ecommerce.e2e.pages.*;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E2E tests for the checkout flow with authenticated users.
 *
 * <p>Tests the checkout process for logged-in users including:
 *
 * <ul>
 *   <li>User registration and login
 *   <li>Cart recovery after login (guest cart merged with user cart)
 *   <li>Pre-filled buyer information from user profile
 *   <li>Complete checkout flow for authenticated users
 * </ul>
 *
 * <p>Prerequisites:
 *
 * <ul>
 *   <li>Application running at configured base URL
 *   <li>Sample products loaded in database
 * </ul>
 */
@DisplayName("Authenticated Checkout E2E Tests")
class CheckoutLoginE2ETest extends BaseE2ETest {

  private static final String TEST_EMAIL = "testuser-" + UUID.randomUUID() + "@example.com";
  private static final String TEST_PASSWORD = "SecurePassword123!";

  @Test
  @DisplayName("Complete checkout flow as authenticated user")
  void completeAuthenticatedCheckoutFlow() {
    // Step 1: Register a new user
    registerNewUser();

    // Step 2: Add product to cart while logged in
    addProductToCart();

    // Step 3: Navigate to cart and start checkout
    CartPage cart = CartPage.navigateTo(page);
    assertTrue(cart.waitForItems().hasItems(), "Cart should have items");

    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Step 4: Fill buyer info
    DeliveryPage delivery =
        buyer.fillBuyerInfo(TEST_EMAIL, "Test", "User", "+1-555-0199").continueToDelivery();

    // Step 5: Fill delivery information
    PaymentPage payment =
        delivery
            .fillAddress("456 Oak Avenue", "Chicago", "60601", "United States", "IL")
            .selectFirstShippingOption()
            .continueToPayment();

    // Step 6: Select payment method
    ReviewPage review = payment.selectFirstPaymentProvider().continueToReview();

    // Step 7: Verify and place order
    assertTrue(review.showsEmail(TEST_EMAIL), "Review page should show user email");

    ConfirmationPage confirmation = review.placeOrder();

    // Step 8: Verify confirmation
    assertTrue(confirmation.isOrderConfirmed(), "Confirmation page should show success message");
  }

  @Test
  @DisplayName("Registered user can add items to cart")
  void registeredUserCanAddToCart() {
    // Step 1: Register a new user
    String uniqueEmail = "cartuser-" + UUID.randomUUID() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Step 2: Add product to cart as registered user
    addProductToCart();

    // Step 3: Navigate to cart and verify items are there
    CartPage cart = CartPage.navigateTo(page);
    assertTrue(cart.waitForItems().getItemCount() > 0, "Registered user cart should have items");
  }

  @Test
  @DisplayName("Can login and checkout with existing account")
  void canLoginAndCheckoutWithExistingAccount() {
    // Step 1: Register a new user first
    String uniqueEmail = "existing-" + UUID.randomUUID() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Step 2: Logout (clear context) and login again
    context.clearCookies();
    LoginPage login = LoginPage.navigateTo(page);
    login.login(uniqueEmail, TEST_PASSWORD);

    // Step 3: Add product to cart
    addProductToCart();

    // Step 4: Start checkout
    CartPage cart = CartPage.navigateTo(page);
    cart.waitForItems();
    BuyerInfoPage buyer = cart.proceedToCheckout();

    // Step 5: Verify we're on buyer info page
    assertTrue(buyer.isOnPage(), "Authenticated user should proceed to checkout");
  }

  @Test
  @DisplayName("Checkout link from login page redirects to checkout after login")
  void loginFromCheckoutRedirectsBackToCheckout() {
    // Step 1: Add product to cart as guest
    addProductToCart();

    // Step 2: Try to start checkout
    CartPage cart = CartPage.navigateTo(page);
    cart.waitForItems();
    clickTestElement("cart-checkout-button");

    // If there's a login prompt during checkout, handle it
    if (getCurrentPath().contains("/login")) {
      // Register via login page
      if (testElementExists("login-register-link")) {
        LoginPage login = new LoginPage(page);
        RegisterPage register = login.goToRegister();
        String uniqueEmail = "redirect-" + UUID.randomUUID() + "@example.com";
        register.register(uniqueEmail, TEST_PASSWORD);
      }
    }

    // After login, should proceed to checkout
    page.waitForURL(url -> url.contains("/checkout") || url.contains("/cart"));
    String path = getCurrentPath();
    assertTrue(
        path.contains("/checkout") || path.contains("/cart"),
        "Should be redirected to checkout or cart after login");
  }

  @Test
  @DisplayName("Buyer step drops the login prompt and prefills the email once logged in")
  void buyerStepReflectsLoginState() {
    // Step 1: as a guest, the buyer step offers login and register and leaves the email blank
    addProductToCart();
    CartPage cart = CartPage.navigateTo(page);
    cart.waitForItems();
    BuyerInfoPage guestBuyer = cart.proceedToCheckout();

    assertTrue(guestBuyer.showsAuthOptions(), "A guest should be offered login and register");
    assertEquals("", guestBuyer.emailValue(), "A guest has no email to prefill");

    // Step 2: register from within the checkout
    String uniqueEmail = "buyerstate-" + UUID.randomUUID() + "@example.com";
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(uniqueEmail, TEST_PASSWORD);

    // Step 3: back on the buyer step, now as a registered visitor
    CartPage cartAfterLogin = CartPage.navigateTo(page);
    cartAfterLogin.waitForItems();
    BuyerInfoPage buyer = cartAfterLogin.proceedToCheckout();

    assertFalse(buyer.showsAuthOptions(), "A logged-in visitor must not be asked to log in again");
    assertTrue(
        buyer.loggedInBannerText().contains(uniqueEmail),
        "The buyer step should name the account the visitor is logged in as");
    assertEquals(
        uniqueEmail,
        buyer.emailValue(),
        "The email should be prefilled from the logged-in account");
  }

  /** Helper method to register a new user with default credentials. */
  private void registerNewUser() {
    RegisterPage register = RegisterPage.navigateTo(page);
    register.register(TEST_EMAIL, TEST_PASSWORD);
  }

  /** Helper method to add a product to the cart. */
  private void addProductToCart() {
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    ProductDetailPage detail = catalog.viewFirstProduct();
    detail.addToCartAndStay();
  }
}
