package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.ReducedMotion;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartTakeOverPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest in a browser that runs no script logs in with items in the cart: the page after the login
 * waits for "Continue" before the cart moves into the account.
 */
@DisplayName("Logging in takes the guest cart over in a step the browser sends")
class CartTakeOverWithoutScriptE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final String MUG = "\"Ubiquitous Language\" Mug";

  @Override
  protected Browser.NewContextOptions contextOptions() {
    // Reduced motion: without a script the page transition's overlay can stay over the page and
    // take the clicks; the shop's CSS drops the transition's animation for reduced motion.
    return new Browser.NewContextOptions()
        .setJavaScriptEnabled(false)
        .setReducedMotion(ReducedMotion.REDUCE);
  }

  @Test
  @DisplayName("Continue takes the guest cart over")
  void continueTakesTheGuestCartIntoTheAccount() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    ProductCatalogPage.navigateTo(page).viewProductNamed(MUG).addToCart();
    LoginPage.navigateTo(page).login(email, PASSWORD);
    CartTakeOverPage takeOver = new CartTakeOverPage(page);
    assertThat(takeOver.message()).hasText("Bringing your cart along.");

    CartPage cart = takeOver.continueToCart();

    assertEquals("/cart", cart.shownPath());
    assertEquals("Welcome back!", cart.successMessage());
    assertEquals(List.of(MUG + " x1"), cart.lineItems());
  }
}
