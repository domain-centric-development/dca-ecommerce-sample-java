package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The API is opened by a token in the request header alone: a browser that is logged in on the
 * shop's pages carries its session cookies along to the API, and they open nothing there.
 */
@DisplayName("API Token Only E2E Tests")
class ApiTrustsOnlyTheTokenE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final String SESSION_COOKIE = "shop-session";

  @Test
  @DisplayName("A page session does not open the api")
  void aBrowserLoggedInOnThePagesIsAStrangerToTheApi() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "ada." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page).login(email, PASSWORD);
    assertEquals("/cart", new CartPage(page).shownPath());
    assertTrue(
        context.cookies().stream().anyMatch(cookie -> SESSION_COOKIE.equals(cookie.name)),
        "the browser holds the pages' session cookie");

    // The browser context's own request client sends the context's cookies, as the browser would.
    APIResponse response = context.request().get(BASE_URL + "/api/carts");

    assertEquals(401, response.status(), response.text());
    assertEquals("Bearer", response.headers().get("www-authenticate"));
  }
}
