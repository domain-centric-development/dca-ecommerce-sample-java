package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A placed order lowers the stock its product page shows, shortly after the order. The scenario
 * names the seeded "Wooden Hexagon Desk Model" with its stock of 8; the test orders a product of
 * its own, created with the same stock, so no other test's order changes what it reads.
 */
@DisplayName("A placed order lowers the stock")
class StockGoesDownE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Stock goes down by the quantity bought")
  void placingAnOrderLowersTheStockByTheQuantityBought() throws Exception {
    final OwnProduct deskModel = OwnProduct.stocked("Stock Goes Down Desk Model", 8);
    navigateTo(deskModel.path());
    final ProductDetailPage product = new ProductDetailPage(page);
    assertEquals(
        "8 units available",
        product.metaValue("Availability"),
        "The desk model starts with the stock it was created with");
    product.addToCart();
    navigateTo(deskModel.path());
    new ProductDetailPage(page).addToCart();
    final ReviewPage review = guestFromTheCartToTheReview();

    review.placeOrder();

    navigateTo(deskModel.path());
    assertEquals(
        "6 units available",
        new ProductDetailPage(page)
            .metaValueOnceItReads("Availability", "6 units available", Duration.ofSeconds(15)));
  }

  private ReviewPage guestFromTheCartToTheReview() {
    return CartPage.navigateTo(page)
        .proceedToCheckout()
        .fillBuyerInfo("jane@example.com", "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery()
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .continueToPayment()
        .chooseTheOfferedProvider()
        .continueToReview();
  }
}
