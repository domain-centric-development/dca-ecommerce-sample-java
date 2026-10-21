package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * A guest's cart after an operator changed the price through the staff API: the line is priced at
 * today's price and says by how much it changed. The shop is one of its own, so the changed price
 * never reaches the catalogue the rest of the suite reads.
 */
@DisplayName("Cart price change")
class CartPriceChangeE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("The cart shows todays price after a change")
  void theCartShowsTodaysPriceAndThatItChangedAfterAPriceChange() throws Exception {
    try (ConfigurableApplicationContext shop = ShopUnderTest.startedWith("cart_price_change_e2e")) {
      final String shopUrl = ShopUnderTest.baseUrlOf(shop);
      final CartPage added =
          ProductCatalogPage.navigateTo(page, shopUrl)
              .viewProductNamed("Domain-Driven Design")
              .addToCart();
      assertEquals(
          "54.99 EUR", added.lineValue("Domain-Driven Design", "Unit Price"), "added at 54.99 EUR");

      assertEquals(
          204,
          ShopUnderTest.changePrice(
              shopUrl,
              ShopUnderTest.staffTokenOf(shop),
              ShopUnderTest.productIdOf(shopUrl, "Domain-Driven Design"),
              "49.99"),
          "the staff API's answer to the new price");

      final CartPage cart = CartPage.navigateTo(page, shopUrl).waitForItems();
      assertEquals(
          "49.99 EUR", cart.lineValue("Domain-Driven Design", "Unit Price"), "the unit price");
      assertThat(cart.priceChangeOf("Domain-Driven Design")).hasText("Price decreased by 5.00 EUR");
      assertThat(cart.priceChangeSummary())
          .hasText("Some items have price changes since they were added to your cart.");
    }
  }
}
