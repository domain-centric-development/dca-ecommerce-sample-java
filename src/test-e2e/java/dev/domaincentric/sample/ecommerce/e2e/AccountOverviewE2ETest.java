package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.AccountOverviewPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A customer who has just logged in opens their account overview from the header. */
@DisplayName("Account Overview E2E Tests")
class AccountOverviewE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final DateTimeFormatter LAST_LOGIN =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneOffset.UTC);

  @Test
  @DisplayName("Overview shows the account")
  void theOverviewShowsTheAddressAndThisLogin() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    Instant beforeLogin = Instant.now();
    LoginPage.navigateTo(page).login(email, PASSWORD);
    Instant afterLogin = Instant.now();

    AccountOverviewPage overview = new SiteHeader(page).followMyAccount();

    assertEquals("/account", overview.shownPath());
    assertEquals("Welcome, " + email, overview.greeting());
    assertEquals("Account Overview", overview.cardTitle());
    assertEquals("Email", overview.emailLabel());
    assertEquals(email, overview.email());
    assertEquals("Last Login", overview.lastLoginLabel());
    // The login's minute is the clock's: the shop records it between the two readings.
    assertTrue(
        List.of(LAST_LOGIN.format(beforeLogin), LAST_LOGIN.format(afterLogin))
            .contains(overview.lastLogin()),
        "the last login is this login in UTC as dd.MM.yyyy HH:mm, was: " + overview.lastLogin());
  }
}
