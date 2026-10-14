package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The buyer step names the account a customer is logged in as and fills in its email. */
@DisplayName("The buyer step knows who is buying")
class BuyerStepKnowsWhoIsBuyingE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Logged in customer sees who is buying")
  void aCustomerWhoRegisteredSeesTheirAccountOnTheBuyerStep() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();
    CartPage.navigateTo(page).waitForItems().proceedToCheckout();
    RegisterPage.navigateTo(page)
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail(email)
        .fillPassword(PASSWORD)
        .fillConfirmPassword(PASSWORD)
        .submit();

    navigateTo("/checkout/buyer");
    BuyerInfoPage buyer = new BuyerInfoPage(page);

    assertEquals("Logged in as " + email, buyer.loggedInBannerText());
    assertFalse(buyer.showsAuthOptions(), "a logged-in customer is not offered a login");
    assertThat(buyer.emailInput()).hasValue(email);
  }
}
