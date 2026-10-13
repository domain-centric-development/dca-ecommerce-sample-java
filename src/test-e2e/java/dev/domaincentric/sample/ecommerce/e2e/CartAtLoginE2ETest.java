package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest who filled a cart and then logs in finds it again in the account's cart. */
@DisplayName("Logging in brings the guest cart along")
class CartAtLoginE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Guest cart follows into the account")
  void theGuestCartFollowsIntoAnAccountWhoseCartHoldsNothing() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    ProductCatalogPage.navigateTo(page).viewProductNamed("\"Ubiquitous Language\" Mug").addToCart();

    LoginPage.navigateTo(page).login(email, PASSWORD);

    CartPage cart = new CartPage(page);
    assertEquals("/cart", cart.shownPath());
    assertEquals("Welcome back!", cart.successMessage());
    assertEquals(List.of("\"Ubiquitous Language\" Mug x1"), cart.lineItems());
  }
}
