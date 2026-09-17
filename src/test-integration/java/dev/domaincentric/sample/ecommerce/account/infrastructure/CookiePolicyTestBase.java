package dev.domaincentric.sample.ecommerce.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Cookie attributes are what a browser reads off the wire, so these tests speak real HTTP through a
 * plain client: MockMvc keeps a cookie as an object and loses {@code SameSite} on the way out.
 */
abstract class CookiePolicyTestBase {

  private static final Pattern PRODUCT_LINK = Pattern.compile("href=\"/products/([0-9a-f-]{36})\"");

  private final HttpClient client =
      HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

  @LocalServerPort private int port;

  /**
   * The {@code Set-Cookie} headers of the product page — it carries the add-to-cart form, and
   * Spring Security writes the CSRF cookie only for a response that actually renders a token.
   */
  protected List<String> setCookieHeadersOfAPageWithAForm() {
    final Matcher link = PRODUCT_LINK.matcher(get("/products").body());
    assertThat(link.find()).as("catalog lists a product").isTrue();

    final List<String> cookies =
        get("/products/" + link.group(1)).headers().allValues("Set-Cookie");
    assertThat(cookies).as("product page sets cookies").isNotEmpty();
    return cookies;
  }

  protected String headerOfProductList(final String name) {
    return get("/products").headers().firstValue(name).orElse(null);
  }

  private HttpResponse<String> get(final String path) {
    try {
      final HttpRequest request =
          HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
      return client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (final IOException e) {
      throw new IllegalStateException("GET " + path + " failed", e);
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("GET " + path + " interrupted", e);
    }
  }
}
