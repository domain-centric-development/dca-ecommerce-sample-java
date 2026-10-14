package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * The cart entry in the header every shop page shares, rendered by the wired application — the
 * visitor's cookies carried from request to request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:header_basket_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Header basket")
class HeaderBasketIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("The header's Cart entry leads to the cart page")
  void headerCartEntryLeadsToTheCartPage() throws Exception {
    final Document catalogue = page("/products");

    final Element cartEntry = catalogue.selectFirst("header [data-test=mini-basket-trigger]");
    assertThat(cartEntry.text()).isEqualTo("Cart");
    final String target = cartEntry.attr("href");
    final Document followed = page(target);

    assertThat(target).isEqualTo("/cart");
    assertThat(followed.selectFirst(".cart__header-title").text()).isEqualTo("Shopping Cart");
  }

  @Test
  @DisplayName("The header's cart entry counts the units in the cart")
  void headerCartEntryCountsTheUnitsInTheCart() throws Exception {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    final Document catalogue = page("/products");

    assertThat(badge(catalogue)).isEqualTo("3");
  }

  @Test
  @DisplayName("The badge follows each add to the cart")
  void badgeFollowsEachAddToTheCart() throws Exception {
    addToCart("Domain-Driven Design");

    final String landedOn = addToCart("Domain-Driven Design");
    final Document cart = page(landedOn);

    assertThat(landedOn).isEqualTo("/cart?notice=product-added");
    assertThat(badge(cart)).isEqualTo("2");
  }

  /** The badge on the header's cart entry. */
  private static String badge(final Document document) {
    final Element badge =
        document.selectFirst(
            "header [data-test=mini-basket-trigger] [data-test=mini-basket-count]");
    assertThat(badge).as("the cart entry's badge").isNotNull();
    return badge.text();
  }

  /**
   * Presses "Add to Cart" on the product page of the product with that name.
   *
   * @return the page the shop redirects to
   */
  private String addToCart(final String productName) throws Exception {
    final Element card =
        page("/products").select("[data-test=product-card]").stream()
            .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled " + productName));
    final Document productPage = page(card.selectFirst("a[data-test=view-product]").attr("href"));
    final Element form = productPage.selectFirst("[data-test=product-add-to-cart-form]");

    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
    return result.getResponse().getRedirectedUrl();
  }

  private Document page(final String path) throws Exception {
    final MvcResult result = perform(get(path));
    assertThat(result.getResponse().getStatus()).as("GET " + path).isEqualTo(200);
    return Jsoup.parse(result.getResponse().getContentAsString());
  }

  private MvcResult perform(final MockHttpServletRequestBuilder request) throws Exception {
    if (!cookies.isEmpty()) {
      request.cookie(cookies.values().toArray(Cookie[]::new));
    }
    final MvcResult result = mockMvc.perform(request).andReturn();
    for (final Cookie cookie : result.getResponse().getCookies()) {
      if (cookie.getMaxAge() == 0) {
        cookies.remove(cookie.getName());
      } else {
        cookies.put(cookie.getName(), cookie);
      }
    }
    return result;
  }
}
