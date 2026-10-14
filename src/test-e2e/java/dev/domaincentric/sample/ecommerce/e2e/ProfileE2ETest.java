package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProfilePage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A logged-in customer changes their email address and date of birth on "My Profile". */
@DisplayName("Profile E2E Tests")
class ProfileE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Saving the profile")
  void savingStoresTheNewAddressAndDateAndKeepsTheCustomerLoggedIn() {
    // The suite's shop keeps its accounts across tests, so both addresses are this run's own.
    UUID run = UUID.randomUUID();
    String email = "anna.schmidt." + run + "@example.com";
    String newEmail = "anna.meyer." + run + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page).login(email, PASSWORD);
    ProfilePage profile = ProfilePage.navigateTo(page);

    ProfilePage saved = profile.fillDetails(newEmail, "1990-05-18").saveExpectingSuccess();

    assertEquals("/account/profile", saved.shownPath());
    assertEquals("Your profile has been updated.", saved.successMessage());
    assertEquals(newEmail, saved.emailValue());
    assertEquals("1990-05-18", saved.dateOfBirthValue());
    assertEquals("Welcome, " + newEmail, new SiteHeader(page).greeting());
  }

  @Test
  @DisplayName("Taken address is refused in the profile")
  void anAddressAnotherAccountUsesIsRefusedAndStaysInTheField() {
    // The suite's shop keeps its accounts across tests, so both addresses are this run's own.
    UUID run = UUID.randomUUID();
    String annasEmail = "anna.schmidt." + run + "@example.com";
    String bensEmail = "ben.weber." + run + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(annasEmail, PASSWORD);
    }
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(bensEmail, PASSWORD);
    }
    LoginPage.navigateTo(page).login(annasEmail, PASSWORD);
    ProfilePage profile = ProfilePage.navigateTo(page);

    ProfilePage refused = profile.fillDetails(bensEmail, "1990-05-17").saveExpectingRefusal();

    assertEquals("This email address is already registered", refused.errorMessage());
    assertEquals(bensEmail, refused.emailValue());
  }
}
