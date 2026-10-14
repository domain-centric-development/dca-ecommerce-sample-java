package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest taking a line out of the cart on the cart page. */
@DisplayName("Remove a Cart Line E2E Tests")
class RemoveCartLineE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Remove takes the line out")
  void removingALineLeavesTheOtherLinesInTheirOrder() {
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");
    addToCart("Team Topologies");
    CartPage cart = CartPage.navigateTo(page).waitForItems();
    assertThat(cart.removeButtonOf("Clean Architecture")).isVisible();

    cart = cart.remove("Clean Architecture");

    assertEquals(List.of("Domain-Driven Design", "Team Topologies"), cart.lineNames());
    assertEquals("2", new SiteHeader(page).basketCount());
  }

  private void addToCart(String productName) {
    ProductCatalogPage.navigateTo(page).viewProductNamed(productName).addToCart();
  }
}
