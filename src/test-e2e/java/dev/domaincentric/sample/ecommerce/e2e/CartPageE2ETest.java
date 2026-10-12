package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** What the cart page shows a guest for each line they collected. */
@DisplayName("Cart Page E2E Tests")
class CartPageE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Cart shows lines with prices")
  void eachLineShowsItsImageQuantityUnitPriceAndLineTotal() {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    CartPage cart = CartPage.navigateTo(page).waitForItems();

    assertTrue(cart.showsImageOf("Domain-Driven Design"));
    assertEquals("2", cart.lineValue("Domain-Driven Design", "Quantity"));
    assertEquals("54.99 EUR", cart.lineValue("Domain-Driven Design", "Unit Price"));
    assertEquals("109.98 EUR", cart.lineValue("Domain-Driven Design", "Line Total"));
    assertTrue(cart.showsImageOf("Clean Architecture"));
    assertEquals("1", cart.lineValue("Clean Architecture", "Quantity"));
    assertEquals("39.99 EUR", cart.lineValue("Clean Architecture", "Unit Price"));
    assertEquals("39.99 EUR", cart.lineValue("Clean Architecture", "Line Total"));
  }

  private void addToCart(String productName) {
    ProductCatalogPage.navigateTo(page).viewProductNamed(productName).addToCart();
  }
}
