package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A logged-in customer's browser names the guest cart of another browser in the cart take-over:
 * only the guest this browser was before the login is taken over, so nothing moves.
 */
@DisplayName("Logging in takes over only the guest cart of this browser")
class GuestCartOfAnotherBrowserE2ETest extends BaseE2ETest {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final String PASSWORD = "Secret123";
  private static final String CLEAN_ARCHITECTURE = "Clean Architecture";
  private static final String IDENTITY_COOKIE = "shop-identity";

  @Test
  @DisplayName("Another guests cart is not taken over")
  void theTakeOverOfAnotherBrowsersGuestCartMovesNothing() throws Exception {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    LoginPage.navigateTo(page).login(email, PASSWORD);
    new CartPage(page);
    try (BrowserContext otherBrowser = browser.newContext()) {
      Page otherGuest = otherBrowser.newPage();
      ProductCatalogPage.navigateTo(otherGuest).viewProductNamed(CLEAN_ARCHITECTURE).addToCart();
      String otherGuestId = guestIdOf(otherBrowser);
      // A page of the shop other than the cart, so that the form's answer is a navigation to wait
      // for.
      ProductCatalogPage.navigateTo(page);

      sendForm(page, "/cart/merge/take-over", Map.of("anonymousUserId", otherGuestId));
      CartPage annasCart = new CartPage(page);

      assertEquals("/cart", annasCart.shownPath());
      assertEquals(List.of(), annasCart.lineItems());
      assertFalse(
          page.locator("[data-test='cart-success-message']").isVisible(),
          "the cart page shows no message");
      assertEquals(
          List.of(CLEAN_ARCHITECTURE + " x1"), CartPage.navigateTo(otherGuest).lineItems());
    }
  }

  /** The guest a browser is: the subject of the identity token its cookie carries. */
  private static String guestIdOf(final BrowserContext context) throws Exception {
    final String token =
        context.cookies().stream()
            .filter(cookie -> IDENTITY_COOKIE.equals(cookie.name))
            .map(cookie -> cookie.value)
            .findFirst()
            .orElseThrow(() -> new AssertionError("the browser holds no " + IDENTITY_COOKIE));
    return JSON.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1])).get("sub").asText();
  }

  /**
   * Sends a form from the page the browser shows, with the page's own CSRF token (from the logout
   * form of a logged-in page) and the given fields, as a form a page could build.
   */
  private static void sendForm(
      final Page page, final String action, final Map<String, String> fields) {
    page.evaluate(
        """
        ([action, fields]) => {
          const form = document.createElement('form');
          form.method = 'POST';
          form.action = action;
          form.appendChild(document.querySelector('[data-test=logout-form] input[type=hidden]').cloneNode());
          for (const [name, value] of Object.entries(fields)) {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = name;
            input.value = value;
            form.appendChild(input);
          }
          document.body.appendChild(form);
          form.submit();
        }
        """,
        List.of(action, fields));
  }
}
