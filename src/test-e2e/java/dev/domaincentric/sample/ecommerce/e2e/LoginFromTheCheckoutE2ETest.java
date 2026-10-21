package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest who logs in from the checkout comes back to the checkout, with the order. */
@DisplayName("Logging in from the checkout returns to the checkout")
class LoginFromTheCheckoutE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final String BUYER_STEP = "/checkout/buyer";

  @Test
  @DisplayName(
      "A guest who logs in from the buyer step returns to the checkout after the cart take-over, with the order")
  void aGuestWhoLogsInFromTheBuyerStepReturnsToTheCheckoutWithTheOrder() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    try (BrowserContext registration = browser.newContext()) {
      RegisterPage.navigateTo(registration.newPage()).register(email, PASSWORD);
    }
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    CartPage.navigateTo(page).proceedToCheckout();

    LoginPage.navigateTo(page, BUYER_STEP).login(email, PASSWORD);

    assertThat(page).hasURL(BASE_URL + BUYER_STEP);
    assertEquals(List.of("Domain-Driven Design x1"), new BuyerInfoPage(page).summaryItems());
  }
}
