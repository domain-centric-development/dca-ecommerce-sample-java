package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A step page opened without an open checkout leads to the cart with a reason.
 *
 * <p>The restart is real, as in {@link ShopRestartE2ETest}: the guest walks to the delivery step in
 * one shop, which then stops; a second one starts on an empty database, and the same browser opens
 * the step there.
 */
@DisplayName("Without an open checkout")
class NoOpenCheckoutE2ETest extends BaseE2ETest {

  private ConfigurableApplicationContext shop;

  @AfterEach
  void stopTheShop() {
    if (shop != null) {
      shop.close();
    }
  }

  @Test
  @DisplayName("Buyer step without a checkout leads to the cart")
  void theBuyerStepWithoutACheckoutLeadsToTheCartWithAMessage() {
    navigateTo("/checkout/buyer");

    assertThat(page).hasURL(BASE_URL + "/cart");
    assertThat(new CartPage(page).errorMessage()).hasText("No active checkout session found");
  }

  @Test
  @DisplayName("Restart of the shop ends an open checkout")
  void aRestartOfTheShopEndsTheOpenCheckout() {
    shop = ShopUnderTest.startedWith("no_open_checkout_before");
    aGuestOnTheDeliveryStepOf(ShopUnderTest.baseUrlOf(shop));
    shop.close();
    shop = ShopUnderTest.startedWith("no_open_checkout_after");
    final String restarted = ShopUnderTest.baseUrlOf(shop);

    page.navigate(restarted + "/checkout/delivery");

    assertThat(page).hasURL(restarted + "/cart");
    assertThat(page.locator("[data-test='cart-error-message']"))
        .hasText("No active checkout session found");
  }

  /**
   * A guest who put "Domain-Driven Design" into the cart of that shop, started the checkout and
   * submitted the buyer step. The page objects know only the suite's shop, so the walk is spelt
   * out.
   */
  private void aGuestOnTheDeliveryStepOf(final String shopUrl) {
    page.navigate(shopUrl + "/products");
    page.locator(
            "[data-test='product-card']:has([data-test='product-card-title']:text-is('Domain-Driven"
                + " Design')) [data-test='view-product']")
        .click();
    page.locator("[data-test='product-add-to-cart-button']").click();
    page.waitForURL(shopUrl + "/cart**");
    page.locator("[data-test='cart-checkout-button']").click();
    page.waitForURL(shopUrl + "/checkout/buyer");
    page.locator("input[name='email']").fill("jane@example.com");
    page.locator("input[name='firstName']").fill("Jane");
    page.locator("input[name='lastName']").fill("Doe");
    page.locator("input[name='phone']").fill("+49 30 1234567");
    page.locator("[data-test='buyer-continue-button']").click();
    assertThat(page).hasURL(shopUrl + "/checkout/delivery");
  }
}
