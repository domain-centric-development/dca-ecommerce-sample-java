package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * The shop inside an iframe on another origin — the case the {@code same-site} and frame-options
 * switch exists for.
 *
 * <p>The embedding page is the shop's own landing page reached under its other name — {@code
 * 127.0.0.1} where the shop under test is {@code localhost} — with an iframe put into it by the
 * test. No second server is needed. The two names are different sites to the browser, so the frame
 * is cross-site exactly as a real foreign host would be, and both stay inside the local network: a
 * page on a public domain may not frame localhost at all (private network access), which would hide
 * the very behaviour under test.
 *
 * <p>Two deployments, two expectations:
 *
 * <ul>
 *   <li>the normal shop refuses to be framed and says so in {@code X-Frame-Options}
 *   <li>the embedded shop ({@code JWT_SAME_SITE=None JWT_SECURE_COOKIES=true}) renders in the
 *       frame, and a form POST from inside it reaches the cart — which is what fails while any of
 *       its cookies stays {@code Lax}. That test runs only against a shop started that way: {@code
 *       ./gradlew test-e2e -De2e.embedded=true}
 * </ul>
 *
 * <p>Same scenario as the .NET sample's {@code EmbeddedShopE2eTest}.
 */
@DisplayName("Embedded Shop E2E Tests")
class EmbeddedShopE2ETest extends BaseE2ETest {

  /**
   * The shop's own address under its other name — a different site to the browser, the same server.
   */
  private static final String OTHER_ORIGIN_URL = BASE_URL.replace("localhost", "127.0.0.1");

  @Test
  @DisplayName("A normal shop refuses to render inside a frame on another origin")
  void framingIsRefusedByDefault() {
    Assumptions.assumeFalse(
        Boolean.getBoolean("e2e.embedded"),
        "the shop under test runs embedded — framing is allowed there by design");
    openEmbeddingPage();

    final Response framed =
        page.waitForResponse(
            response -> response.url().equals(BASE_URL + "/products"),
            () -> frameTheShop("/products"));

    assertEquals(
        "SAMEORIGIN",
        framed.allHeaders().get("x-frame-options"),
        "the shop tells the browser to refuse the frame");
    assertFalse(
        framedShop().locator("[data-test='product-card']").first().isVisible(),
        "the shop must not render inside a foreign frame");
  }

  @Test
  @EnabledIfSystemProperty(
      named = "e2e.embedded",
      matches = "true",
      disabledReason =
          "needs a shop started with JWT_SAME_SITE=None JWT_SECURE_COOKIES=true; run with"
              + " -De2e.embedded=true")
  @DisplayName("An embedded shop accepts a form POST made from inside the foreign frame")
  void formPostFromAForeignFrameReachesTheCart() {
    openEmbeddingPage();
    frameTheShop("/products");

    final FrameLocator shop = framedShop();
    final Locator firstProduct = shop.locator("[data-test='view-product']").first();
    firstProduct.waitFor();
    firstProduct.click();

    // Adding to the cart is a form POST carrying the CSRF token. It arrives complete only when the
    // identity cookie and the token cookie both travel into the frame.
    shop.locator("[data-test='product-add-to-cart-button']").click();

    final Locator cartItems = shop.locator("[data-test='cart-item']");
    cartItems.first().waitFor();
    assertTrue(cartItems.count() >= 1, "the product reached the cart of the framed shop");
  }

  /** Opens a page on the other origin — any page of it will do, it only has to host the frame. */
  private void openEmbeddingPage() {
    page.navigate(OTHER_ORIGIN_URL + "/");
  }

  /** Puts the shop into a frame of that page, the way a foreign site would embed it. */
  private void frameTheShop(final String shopPath) {
    page.evaluate(
        """
        src => {
            const frame = document.createElement('iframe');
            frame.id = 'shop';
            frame.src = src;
            frame.width = 1000;
            frame.height = 800;
            document.body.appendChild(frame);
        }
        """,
        BASE_URL + shopPath);
  }

  private FrameLocator framedShop() {
    return page.frameLocator("#shop");
  }
}
