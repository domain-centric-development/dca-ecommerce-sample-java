package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Response;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Who may put the shop in a frame and still use it — decided by how the shop was started.
 *
 * <p>Out of the box a page on another port of the shop's host frames the shop and uses it. A
 * foreign site ({@code 127.0.0.1} against {@code localhost}) may frame the shop but gets nowhere
 * with it: the browser withholds the shop's {@code Lax} cookies there. A shop started for
 * cross-site embedding sends them as {@code SameSite=None; Secure} and works in that frame; a shop
 * started with framing switched off is shown in no other page's frame at all. The two shops started
 * otherwise run in this process beside the one the suite drives, each with its own settings and
 * database.
 */
@DisplayName("Shop in a Frame E2E Tests")
class ShopInAFrameE2ETest extends BaseE2ETest {

  private HttpServer embeddingServer;
  private ConfigurableApplicationContext startedOtherwise;

  @AfterEach
  void stopWhatTheTestStarted() {
    if (embeddingServer != null) {
      embeddingServer.stop(0);
      embeddingServer = null;
    }
    if (startedOtherwise != null) {
      startedOtherwise.close();
      startedOtherwise = null;
    }
  }

  @Test
  @DisplayName("A page on another port embeds the shop")
  void framesTheShopFromAnotherPortAndAddsToTheCart() {
    page.navigate(startEmbeddingServer("localhost", BASE_URL + "/products"));
    final FrameLocator shop = page.frameLocator("#shop");

    final String product = openFirstProductAndPressAddToCart(shop);

    final Locator names = shop.locator("[data-test='cart-item-name']");
    assertThat(names.first()).isVisible();
    assertTrue(
        names.allInnerTexts().stream().map(String::strip).toList().contains(product),
        "the framed shop's cart page shows " + product);
  }

  @Test
  @DisplayName("A foreign site cannot add to the cart")
  void aForeignSiteCannotAddToTheCart() {
    Assumptions.assumeTrue(
        BASE_URL.contains("localhost"),
        "the cross-site case pairs localhost with 127.0.0.1; point e2e.baseUrl at localhost");
    page.navigate(startEmbeddingServer("127.0.0.1", BASE_URL + "/products"));
    final FrameLocator shop = page.frameLocator("#shop");

    openFirstProductAndPressAddToCart(shop);

    page.evaluate("src => document.getElementById('shop').src = src", BASE_URL + "/cart");
    shop.locator("[data-test='cart-items'], [data-test='cart-browse-link']").first().waitFor();
    assertEquals(
        0,
        shop.locator("[data-test='cart-item']").count(),
        "the product did not reach the framed shop's cart");
  }

  @Test
  @DisplayName("A shop started for embedding works cross site")
  void aShopStartedForEmbeddingWorksCrossSite() {
    startedOtherwise =
        ShopUnderTest.startedWith(
            "shop_in_a_frame_cross_site",
            "app.security.jwt.same-site=None",
            "app.security.jwt.secure-cookies=true");
    final String shopUrl = ShopUnderTest.baseUrlOf(startedOtherwise);
    page.navigate(startEmbeddingServer("127.0.0.1", shopUrl + "/products"));
    final FrameLocator shop = page.frameLocator("#shop");

    final String product = openFirstProductAndPressAddToCart(shop);

    final Locator names = shop.locator("[data-test='cart-item-name']");
    assertThat(names.first()).isVisible();
    assertTrue(
        names.allInnerTexts().stream().map(String::strip).toList().contains(product),
        "the framed shop's cart page shows " + product);
  }

  @Test
  @DisplayName("A shop started without framing refuses the frame")
  void aShopStartedWithoutFramingRefusesTheFrame() {
    startedOtherwise =
        ShopUnderTest.startedWith(
            "shop_in_a_frame_no_framing", "app.security.jwt.allow-framing=false");
    final String shopUrl = ShopUnderTest.baseUrlOf(startedOtherwise);
    final String embeddingPage = startEmbeddingServer("localhost", shopUrl + "/products");

    final Response catalogue =
        page.waitForResponse(
            response -> response.url().startsWith(shopUrl + "/products"),
            () -> page.navigate(embeddingPage));

    assertEquals("SAMEORIGIN", catalogue.headerValue("X-Frame-Options"));
    final List<Frame> frames = page.mainFrame().childFrames();
    assertEquals(1, frames.size(), "the embedding page has its frame");
    assertEquals(
        0,
        frames.get(0).locator("[data-test]").count(),
        "the browser shows nothing of the shop inside the frame");
  }

  /**
   * Opens the first product of the framed catalogue and presses "Add to Cart", waiting for the
   * shop's answer to that press.
   *
   * @return the name of the product, as its page shows it
   */
  private String openFirstProductAndPressAddToCart(final FrameLocator shop) {
    final Locator firstProduct = shop.locator("[data-test='view-product']").first();
    assertThat(firstProduct).isVisible();
    firstProduct.click();

    final String product = shop.locator("[data-test='product-detail-title']").innerText().strip();
    page.waitForResponse(
        response -> "POST".equals(response.request().method()),
        () -> shop.locator("[data-test='product-add-to-cart-button']").click());
    return product;
  }

  /**
   * A throwaway server on a free port of this host, serving nothing but a page that frames the
   * shop. A real origin rather than an intercepted one, as in {@link EmbeddedShopE2ETest}.
   *
   * @return the address of that page
   */
  private String startEmbeddingServer(final String host, final String framedAddress) {
    try {
      embeddingServer = HttpServer.create(new InetSocketAddress(host, 0), 0);
      final byte[] body =
          """
          <!doctype html>
          <title>A page that frames the shop</title>
          <iframe id="shop" src="%s" width="1000" height="800"></iframe>
          """
              .formatted(framedAddress)
              .getBytes(StandardCharsets.UTF_8);
      embeddingServer.createContext(
          "/",
          exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
              out.write(body);
            }
          });
      embeddingServer.start();
      return "http://" + host + ":" + embeddingServer.getAddress().getPort() + "/";
    } catch (final IOException e) {
      throw new IllegalStateException("could not start the embedding server", e);
    }
  }
}
