package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A customer with an account logs in on the login page and lands in their cart. */
@DisplayName("Login E2E Tests")
class LoginE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Login opens the cart")
  void loginOpensTheCartAndGreetsTheCustomer() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }

    LoginPage.navigateTo(page).login(email, PASSWORD);

    CartPage cart = new CartPage(page);
    assertEquals("/cart", cart.shownPath());
    assertEquals("Welcome back!", cart.successMessage());
    SiteHeader header = new SiteHeader(page);
    assertEquals("Welcome, " + email, header.greeting());
    assertEquals("Logout", header.logoutButton());
    assertFalse(header.offersLogin(), "a logged-in customer is not offered to log in");
    assertFalse(header.offersRegister(), "a logged-in customer is not offered to register");
  }

  @Test
  @DisplayName("An outside return page opens the cart after login")
  void aLoginOpensTheCartInsteadOfAReturnPageOfAnotherSite() {
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }

    LoginPage.navigateTo(page, "https://elsewhere.example/").login(email, PASSWORD);

    CartPage cart = new CartPage(page);
    assertEquals("/cart", cart.shownPath());
    assertEquals("Welcome back!", cart.successMessage());
  }

  @Test
  @DisplayName("Wrong password is refused")
  void aWrongPasswordIsRefusedWithTheGenericMessageAndKeepsTheEmail() {
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }

    LoginPage refused =
        LoginPage.navigateTo(page).fillCredentials(email, "Secret999").submitExpectingError();

    assertEquals("Invalid email or password", refused.errorMessage());
    assertEquals(email, refused.emailValue());
    assertEquals("", refused.passwordValue());
    SiteHeader header = new SiteHeader(page);
    assertTrue(header.offersLogin(), "a refused visitor is still offered to log in");
    assertTrue(header.offersRegister(), "a refused visitor is still offered to register");
  }
}
