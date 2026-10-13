package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A guest keeps changing the cart while a checkout is open: the open checkout keeps the order it
 * was started with.
 */
@DisplayName("The open checkout keeps its order while the cart changes")
class OpenCheckoutKeepsItsOrderE2ETest extends BaseE2ETest {

  private static final String PIN = "\"Bounded Context\" Enamel Pin";
  private static final String HOODIE = "\"Domain over Framework\" Hoodie";

  @Test
  @DisplayName("Open checkout keeps its summary")
  void theBuyerStepStillShowsTheOrderTheCheckoutWasStartedWith() {
    addToCart(PIN, 2);
    CartPage.navigateTo(page).proceedToCheckout();
    addToCart(PIN, 3);
    addToCart(HOODIE, 1);
    assertEquals(
        List.of(PIN + " x5", HOODIE + " x1"),
        CartPage.navigateTo(page).lineItems(),
        "The cart shows two lines with the pin five times");

    navigateTo("/checkout/buyer");

    assertEquals(List.of(PIN + " x2"), new BuyerInfoPage(page).summaryItems());
  }

  private void addToCart(final String productName, final int times) {
    for (int i = 0; i < times; i++) {
      ProductCatalogPage.navigateTo(page).viewProductNamed(productName).addToCart();
    }
  }
}
