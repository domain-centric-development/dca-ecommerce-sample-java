package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest who filled a cart and then registers finds it as the new account's cart. */
@DisplayName("The cart stays with the person")
class CartAtRegistrationE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";

  @Test
  @DisplayName("Registering keeps the guest cart")
  void theNewAccountTakesOverTheCartTheGuestFilled() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + UUID.randomUUID() + "@example.com";
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();

    RegisterPage.navigateTo(page)
        .fillOwner("Anna", "Schmidt", "1990-05-17")
        .fillEmail(email)
        .fillPassword(PASSWORD)
        .fillConfirmPassword(PASSWORD)
        .submit();

    CartPage cart = CartPage.navigateTo(page);
    assertEquals(List.of("Domain-Driven Design x1"), cart.lineItems());
    assertEquals("Welcome, " + email, new SiteHeader(page).greeting());
  }
}
