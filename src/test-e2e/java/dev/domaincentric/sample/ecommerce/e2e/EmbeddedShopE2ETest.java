package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * The shop inside someone else's iframe — a slide deck, a docs page, a demo.
 *
 * <p>Two cases, and the difference is what "someone else" means to the browser:
 *
 * <ul>
 *   <li><b>Another port of the same host.</b> A different <em>origin</em>, so the shop has to allow
 *       framing — which it does out of the box — but the same <em>site</em>, so its {@code Lax}
 *       cookies travel into the frame unchanged. This is the slide-deck case and needs no
 *       configuration at all.
 *   <li><b>Another site.</b> {@code 127.0.0.1} against {@code localhost} here. Now the cookies stay
 *       behind unless the shop is started for it ({@code JWT_SAME_SITE=None
 *       JWT_SECURE_COOKIES=true}), so that case runs only against such a shop and skips otherwise.
 * </ul>
 *
 * <p>Both embedding pages come from a throwaway HTTP server the test starts on a free port, bound
 * to {@code localhost} for the first case and to {@code 127.0.0.1} for the second. A real origin
 * rather than an intercepted one, because a browser refuses to frame anything on the local network
 * from a page whose own origin it could not resolve. Same scenarios as the .NET sample's {@code
 * EmbeddedShopE2eTest}.
 */
@DisplayName("Embedded Shop E2E Tests")
class EmbeddedShopE2ETest extends BaseE2ETest {

  private HttpServer embeddingServer;

  @AfterEach
  void stopEmbeddingServer() {
    if (embeddingServer != null) {
      embeddingServer.stop(0);
      embeddingServer = null;
    }
  }

  @Test
  @DisplayName("A slide deck on another port frames the shop and adds to the cart")
  void framedByAnotherPortOfTheSameHost() {
    page.navigate(startEmbeddingServer("localhost", "/products"));

    addFirstProductToTheCartInTheFrame();
  }

  @Test
  @EnabledIfSystemProperty(
      named = "e2e.embedded",
      matches = "true",
      disabledReason =
          "needs a shop started with JWT_SAME_SITE=None JWT_SECURE_COOKIES=true; run with"
              + " -De2e.embedded=true")
  @DisplayName("A page on another site frames the shop and adds to the cart")
  void framedByAnotherSite() {
    Assumptions.assumeTrue(
        BASE_URL.contains("localhost"),
        "the cross-site case pairs localhost with 127.0.0.1; point e2e.baseUrl at localhost to run it");

    // 127.0.0.1 is the same machine under a name the browser counts as a different site — which is
    // what makes the shop's cookies cross-site here, unlike the port-only difference above.
    page.navigate(startEmbeddingServer("127.0.0.1", "/products"));

    addFirstProductToTheCartInTheFrame();
  }

  /**
   * The whole point of framing the shop: the visitor can still use it. Adding to the cart is a form
   * POST carrying the CSRF token, so it only arrives complete when the identity cookie and the
   * token cookie both travelled into the frame.
   */
  private void addFirstProductToTheCartInTheFrame() {
    final FrameLocator shop = page.frameLocator("#shop");
    final Locator firstProduct = shop.locator("[data-test='view-product']").first();
    firstProduct.waitFor();
    firstProduct.click();

    shop.locator("[data-test='product-add-to-cart-button']").click();

    final Locator cartItems = shop.locator("[data-test='cart-item']");
    cartItems.first().waitFor();
    assertTrue(cartItems.count() >= 1, "the product reached the cart of the framed shop");
  }

  /**
   * A throwaway server on a free port of this host, serving nothing but the embedding page. A real
   * origin rather than an intercepted one: a browser refuses to frame anything on the local network
   * from a page whose own origin it could not resolve.
   *
   * @return the address of that page
   */
  private String startEmbeddingServer(final String host, final String shopPath) {
    try {
      embeddingServer = HttpServer.create(new InetSocketAddress(host, 0), 0);
      final byte[] page = embeddingPage(shopPath).getBytes(StandardCharsets.UTF_8);
      embeddingServer.createContext(
          "/",
          exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, page.length);
            try (OutputStream body = exchange.getResponseBody()) {
              body.write(page);
            }
          });
      embeddingServer.start();
      return "http://" + host + ":" + embeddingServer.getAddress().getPort() + "/";
    } catch (final IOException e) {
      throw new IllegalStateException("could not start the embedding server", e);
    }
  }

  private static String embeddingPage(final String shopPath) {
    return """
        <!doctype html>
        <title>A page that frames the shop</title>
        <iframe id="shop" src="%s%s" width="1000" height="800"></iframe>
        """
        .formatted(BASE_URL, shopPath);
  }
}
