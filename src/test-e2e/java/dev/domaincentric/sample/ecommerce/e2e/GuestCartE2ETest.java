package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.options.Cookie;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A guest's cart belongs to their browser and waits there for the next visit. */
@DisplayName("Guest Cart E2E Tests")
class GuestCartE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Cart kept across visits")
  void theCartIsThereAgainWhenTheGuestComesBackTheNextDay() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("\"Ubiquitous Language\" Mug").addToCart();

    closeTheBrowserAndComeBackAfter(Duration.ofDays(1));
    CartPage cart = CartPage.navigateTo(page);

    assertEquals(List.of("\"Ubiquitous Language\" Mug x1"), cart.lineItems());
  }

  /**
   * Closes the browser and opens it again later: what it keeps are the cookies still valid then — a
   * session cookie, or one expiring sooner, is gone.
   */
  private void closeTheBrowserAndComeBackAfter(Duration away) {
    double returnsAt = Instant.now().plus(away).getEpochSecond();
    List<Cookie> kept =
        context.cookies().stream()
            .filter(cookie -> cookie.expires != null && cookie.expires > returnsAt)
            .toList();
    context.close();

    context = browser.newContext(contextOptions());
    context.addCookies(kept);
    page = context.newPage();
  }
}
