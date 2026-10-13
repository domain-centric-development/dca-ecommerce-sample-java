package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartMergePage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.LoginPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest whose cart and whose account's cart both hold items chooses how they come together. */
@DisplayName("Choose how two carts come together")
class CartMergeChoiceE2ETest extends BaseE2ETest {

  private static final String PASSWORD = "Secret123";
  private static final String CLEAN_ARCHITECTURE = "Clean Architecture";
  private static final String MUG = "\"Ubiquitous Language\" Mug";

  @Test
  @DisplayName("Merge both adds up the carts")
  void mergingBothCartsAddsUpTheQuantitiesOfTheSameProduct() {
    // The suite's shop keeps its accounts across tests, so the account gets an address of its own.
    String email = "anna.schmidt." + System.currentTimeMillis() + "@example.com";
    try (BrowserContext elsewhere = browser.newContext()) {
      Page accountBrowser = elsewhere.newPage();
      RegisterPage.navigateTo(accountBrowser).register(email, PASSWORD);
      ProductCatalogPage.navigateTo(accountBrowser)
          .viewProductNamed(CLEAN_ARCHITECTURE)
          .addToCart();
    }
    ProductCatalogPage.navigateTo(page).viewProductNamed(CLEAN_ARCHITECTURE).addToCart();
    ProductCatalogPage.navigateTo(page).viewProductNamed(CLEAN_ARCHITECTURE).addToCart();
    ProductCatalogPage.navigateTo(page).viewProductNamed(MUG).addToCart();
    LoginPage.navigateTo(page).login(email, PASSWORD);
    CartMergePage mergePage = new CartMergePage(page);

    CartPage cart = mergePage.mergeBothCarts();

    assertEquals("/cart", cart.shownPath());
    assertEquals("Carts merged successfully!", cart.successMessage());
    assertEquals(List.of(CLEAN_ARCHITECTURE + " x3", MUG + " x1"), cart.lineItems());
  }
}
