package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.ChangePasswordPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A logged-in customer replaces their password on the change-password page. */
@DisplayName("Change Password E2E Tests")
class ChangePasswordE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final String NEW_PASSWORD = "Better456";

  @Test
  @DisplayName("Changing the password")
  void theChangeIsConfirmedOnTheEmptiedFormAndTheCustomerStaysLoggedIn() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page).login(email, PASSWORD);
    ChangePasswordPage form = ChangePasswordPage.navigateTo(page);

    ChangePasswordPage changed = form.changePassword(PASSWORD, NEW_PASSWORD, NEW_PASSWORD);

    assertEquals("/account/change-password", changed.shownPath());
    assertEquals("Your password has been changed.", changed.successMessage());
    assertEquals("", changed.currentPasswordValue());
    assertEquals("", changed.newPasswordValue());
    assertEquals("", changed.confirmPasswordValue());
    assertEquals("Welcome, " + email, new SiteHeader(page).greeting());
  }
}
