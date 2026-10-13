package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Each page on the way to the checkout at a 393 px viewport, one page per test: the document never
 * scrolls sideways.
 */
@DisplayName("Shop Fits A Phone E2E Tests")
class ShopFitsAPhoneE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setViewportSize(393, 852);
  }

  @Test
  @DisplayName("The catalogue fits a phone")
  void theCataloguePageDoesNotScrollSidewaysOnAPhone() {
    ProductCatalogPage catalogue = ProductCatalogPage.navigateTo(page);

    assertTrue(catalogue.fitsViewport(), "Catalogue page scrolls sideways at 393 px");
  }

  @Test
  @DisplayName("A product page fits a phone")
  void aProductPageDoesNotScrollSidewaysOnAPhone() {
    ProductDetailPage product =
        ProductCatalogPage.navigateTo(page)
            .viewProductNamed("Patterns of Enterprise Application Architecture");

    assertTrue(product.fitsViewport(), "Product page scrolls sideways at 393 px");
  }

  @Test
  @DisplayName("The cart fits a phone")
  void theCartPageDoesNotScrollSidewaysOnAPhone() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();

    CartPage cart = CartPage.navigateTo(page);

    assertTrue(cart.fitsViewport(), "Cart page scrolls sideways at 393 px");
  }

  @Test
  @DisplayName("The buyer step fits a phone")
  void theBuyerStepOpensAndDoesNotScrollSidewaysOnAPhone() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();

    BuyerInfoPage buyer = CartPage.navigateTo(page).proceedToCheckout();

    assertThat(page).hasURL(BASE_URL + "/checkout/buyer");
    assertTrue(buyer.fitsViewport(), "Buyer step scrolls sideways at 393 px");
  }
}
