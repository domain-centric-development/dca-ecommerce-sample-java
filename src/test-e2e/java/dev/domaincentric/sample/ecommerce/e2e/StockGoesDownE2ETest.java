package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A placed order lowers the stock its product page shows, shortly after the order. */
@DisplayName("A placed order lowers the stock")
class StockGoesDownE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Stock goes down by the quantity bought")
  void placingAnOrderLowersTheStockByTheQuantityBought() {
    final ProductDetailPage product =
        ProductCatalogPage.navigateTo(page).viewProductNamed(DESK_MODEL);
    final String productPath = product.shownPath();
    assertEquals(
        "8 units available",
        product.metaValue("Availability"),
        "The desk model starts with its seeded stock");
    product.addToCart();
    navigateTo(productPath);
    new ProductDetailPage(page).addToCart();
    final ReviewPage review = guestFromTheCartToTheReview();

    review.placeOrder();

    navigateTo(productPath);
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
