package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest starts the checkout straight from the cart page, without an account. */
@DisplayName("Start Checkout E2E Tests")
class StartCheckoutE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Proceed opens the buyer step")
  void proceedingFromTheCartOpensTheBuyerStepWithoutAskingForAnAccount() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();

    CartPage.navigateTo(page).pressProceedToCheckout();

    assertThat(page).hasURL(BASE_URL + "/checkout/buyer");
    assertThat(page.locator("input[type=password]")).hasCount(0);
  }
}
