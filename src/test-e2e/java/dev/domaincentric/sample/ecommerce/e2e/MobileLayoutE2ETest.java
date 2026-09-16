package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The shop on a phone: at a 393 px viewport (iPhone 14 Pro) the catalog, the cart and the checkout
 * entry fit the screen — the document never scrolls sideways.
 *
 * <p>Same scenario and page objects as the .NET sample's {@code MobileLayoutE2eTest}; either suite
 * can be pointed at either shop.
 */
@DisplayName("Mobile Layout E2E Tests")
class MobileLayoutE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setViewportSize(393, 852);
  }

  @Test
  @DisplayName(
      "On a phone the catalog, a product and the cart fit the screen without sideways scrolling")
  void shopFitsAPhoneViewport() {
    ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    assertTrue(catalog.fitsViewport(), "Catalog page scrolls sideways at 393 px");

    ProductDetailPage product = catalog.viewFirstProduct();
    assertTrue(product.fitsViewport(), "Product page scrolls sideways at 393 px");

    CartPage cart = product.addToCart();
    assertTrue(cart.fitsViewport(), "Cart page scrolls sideways at 393 px");

    BuyerInfoPage buyer = cart.proceedToCheckout();
    assertTrue(buyer.fitsViewport(), "Checkout page scrolls sideways at 393 px");
  }
}
