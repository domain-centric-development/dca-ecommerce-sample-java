package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** What the header's cart entry reveals to a guest who hovers it. */
@DisplayName("Header Basket E2E Tests")
class HeaderBasketE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Basket dropdown lists the cart")
  void hoveringTheHeaderBasketListsTheLinesTheirTotalAndAWayToTheCart() {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");
    ProductCatalogPage.navigateTo(page);

    SiteHeader header = new SiteHeader(page).hoverBasket();

    assertEquals("x2", header.basketQuantityOf("Domain-Driven Design"));
    assertEquals("x1", header.basketQuantityOf("Clean Architecture"));
    assertEquals("Total", header.basketTotalLabel());
    assertEquals("149.97 EUR", header.basketTotal());
    assertEquals("View Cart", header.basketViewCartLabel());
    assertEquals("/cart", header.basketViewCartTarget());
  }

  private void addToCart(String productName) {
    ProductCatalogPage.navigateTo(page).viewProductNamed(productName).addToCart();
  }
}
