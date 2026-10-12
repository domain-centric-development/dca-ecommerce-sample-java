package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest putting a product into the cart from its product page. */
@DisplayName("Add to Cart E2E Tests")
class AddToCartE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Add to cart lands on the cart")
  void addToCartOpensTheCartWithOneUnitOfTheProduct() {
    CartPage cart =
        ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design").addToCart();

    assertEquals("/cart", cart.shownPath());
    assertEquals("Product added to cart!", cart.successMessage());
    assertEquals(List.of("Domain-Driven Design x1"), cart.lineItems());
  }
}
