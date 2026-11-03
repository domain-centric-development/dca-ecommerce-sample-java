package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A customer who is logged in and opens the login page is asked to log out first. */
@DisplayName("Login while logged in")
class LoginWhileLoggedInE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("The login page asks a logged in customer to log out")
  void theLoginPageAsksALoggedInCustomerToLogOutFirst() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    // Registering signs this browser in.
    RegisterPage.navigateTo(page).register(email, PASSWORD);

    LoginPage login = LoginPage.navigateTo(page);

    assertEquals(
        "You are logged in as " + email + ". Please log out first.", login.loggedInMessage());
    assertEquals("Logout", login.logoutButton());
    assertFalse(login.showsLoginForm(), "a logged-in customer is shown no login form");
  }
}
