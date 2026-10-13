package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.ChangePasswordPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A logged-in customer's password change is refused on the change-password page. */
@DisplayName("Refused Password Change E2E Tests")
class RefusedPasswordChangeE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Differing confirmation is refused")
  void aConfirmationThatDiffersFromTheNewPasswordIsRefusedOnTheEmptiedForm() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page).login(email, PASSWORD);
    ChangePasswordPage form = ChangePasswordPage.navigateTo(page);

    ChangePasswordPage refused = form.submitRefused(PASSWORD, "Better456", "Better457");

    assertEquals("New password and confirmation do not match", refused.errorMessage());
    assertEquals("", refused.currentPasswordValue());
    assertEquals("", refused.newPasswordValue());
    assertEquals("", refused.confirmPasswordValue());
  }
}
