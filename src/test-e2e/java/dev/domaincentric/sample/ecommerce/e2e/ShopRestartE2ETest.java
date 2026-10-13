package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.RegisterPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A customer logged in, and the shop was restarted since: the shop keeps nothing across a restart,
 * so the same browser comes back as a visitor.
 *
 * <p>The restart is real: the test starts a shop, logs the customer in there, stops it and starts
 * it again on an empty database. Both run on {@code localhost}, so the browser sends the cookies of
 * the first to the second, as it would to a restarted shop.
 */
@DisplayName("Shop restart E2E Tests")
class ShopRestartE2ETest extends BaseE2ETest {

  private static final String EMAIL = "anna.schmidt@example.com";
  private static final String PASSWORD = "Secret123";

  private ConfigurableApplicationContext shop;

  @AfterEach
  void stopTheShop() {
    if (shop != null) {
      shop.close();
    }
  }

  @Test
  @DisplayName("Restart turns the customer into a visitor")
  void aCustomerLoggedInBeforeARestartBrowsesTheCatalogueAsAVisitor() {
    shop = ShopUnderTest.startedWith("shop_restart_before");
    RegisterPage.navigateTo(page, ShopUnderTest.baseUrlOf(shop)).register(EMAIL, PASSWORD);
    assertEquals("Welcome, " + EMAIL, new SiteHeader(page).greeting(), "logged in before");
    shop.close();
    shop = ShopUnderTest.startedWith("shop_restart_after");

    ProductCatalogPage catalogue =
        ProductCatalogPage.navigateTo(page, ShopUnderTest.baseUrlOf(shop));

    assertTrue(catalogue.hasProducts(), "the page shows the catalogue");
    assertFalse(catalogue.showsErrorMessage(), "the page shows no error message");
    SiteHeader header = new SiteHeader(page);
    assertTrue(header.offersLogin(), "the header offers to log in");
    assertTrue(header.offersRegister(), "the header offers to register");
    assertFalse(header.greets(), "the header no longer welcomes the customer");
  }
}
