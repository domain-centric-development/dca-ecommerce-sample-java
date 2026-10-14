package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
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
   * submitted the buyer step.
   */
  private void aGuestOnTheDeliveryStepOf(final String shopUrl) {
    ProductCatalogPage.navigateTo(page, shopUrl)
        .viewProductNamed("Domain-Driven Design")
        .addToCart()
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery();
  }
}
