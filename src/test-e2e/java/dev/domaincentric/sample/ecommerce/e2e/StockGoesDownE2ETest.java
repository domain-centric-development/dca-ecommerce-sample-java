package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ReviewPage;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A placed order lowers the stock its product page shows, shortly after the order. The test runs a
 * shop of its own, started with nothing but the seed: it reads the seeded stock of the "Wooden
 * Hexagon Desk Model" exactly and uses some of it up, which the suite's shop keeps for other tests.
 */
@DisplayName("A placed order lowers the stock")
class StockGoesDownE2ETest extends BaseE2ETest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";

  @Test
  @DisplayName("Stock goes down by the quantity bought")
  void placingAnOrderLowersTheStockByTheQuantityBought() {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("stock_goes_down_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final ProductDetailPage product =
          ProductCatalogPage.navigateTo(page, shopUrl).viewProductNamed(DESK_MODEL);
      final String productPath = product.shownPath();
      assertEquals(
          "8 units available",
          product.metaValue("Availability"),
          "The desk model starts with its seeded stock");
      product.addToCart();
      ProductDetailPage.navigateTo(page, shopUrl, productPath).addToCart();
      final ReviewPage review = guestFromTheCartToTheReview(shopUrl);

      review.placeOrder();

      assertEquals(
          "6 units available",
          ProductDetailPage.navigateTo(page, shopUrl, productPath)
              .metaValueOnceItReads("Availability", "6 units available", Duration.ofSeconds(15)));
    }
  }

  private ReviewPage guestFromTheCartToTheReview(final String shopUrl) {
    return CartPage.navigateTo(page, shopUrl)
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
