package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest places an order while the cart holds more than the checkout was started with: the order
 * takes what it bought out of the cart, shortly after it is placed, and leaves the rest.
 */
@DisplayName("After the order the cart keeps only what was not bought")
class CartAfterTheOrderE2ETest extends BaseE2ETest {

  private static final String PIN = "\"Bounded Context\" Enamel Pin";
  private static final String HOODIE = "\"Domain over Framework\" Hoodie";
  private static final String T_SHIRT = "\"Ports & Adapters\" T-Shirt";

  @Test
  @DisplayName("Only the later addition remains")
  void theCartKeepsOnlyWhatWasAddedAfterTheCheckoutStarted() {
    addToCart(PIN, 2);
    addToCart(HOODIE, 1);
    CartPage.navigateTo(page).proceedToCheckout();
    addToCart(T_SHIRT, 1);
    assertEquals(3, CartPage.navigateTo(page).getItemCount(), "The cart holds three lines");

    navigateTo("/checkout/buyer");
    new BuyerInfoPage(page)
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery()
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .continueToPayment()
        .chooseTheOfferedProvider()
        .continueToReview()
        .pressPlaceOrder();

    assertThat(page.locator("[data-test='confirmation-title']"))
        .hasText("Your order has been placed successfully");
    assertEquals(List.of(T_SHIRT + " x1"), cartOnceItHoldsLines(1).lineItems());
  }

  /**
   * The cart page once it shows that many lines, or as it is after 15 seconds — the order reaches
   * the cart as an event after it is placed, so the page is opened again until then.
   */
  private CartPage cartOnceItHoldsLines(final int lines) {
    try {
      page.waitForCondition(
          () -> CartPage.navigateTo(page).getItemCount() == lines,
          new Page.WaitForConditionOptions().setTimeout(15_000));
    } catch (TimeoutError notYet) {
      // the assertion on the page names what the cart shows instead
    }
    return new CartPage(page);
  }

  private void addToCart(final String productName, final int times) {
    for (int i = 0; i < times; i++) {
      ProductCatalogPage.navigateTo(page).viewProductNamed(productName).addToCart();
    }
  }
}
