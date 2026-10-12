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
 * The cart page a guest reaches after putting a product into the cart, rendered by the wired
 * application — the visitor's cookies carried from request to request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Cart page")
class CartPageIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("The cart page is titled and headed Shopping Cart")
  void cartPageIsTitledAndHeadedShoppingCart() throws Exception {
    addToCart("Domain-Driven Design");

    final Document cart = page("/cart");

    assertThat(cart.title()).isEqualTo("Shopping Cart");
    assertThat(cart.selectFirst(".cart__header-title").text()).isEqualTo("Shopping Cart");
  }

  @Test
  @DisplayName("Above the lines the cart counts its lines and its units")
  void cartCountsItsLinesAndItsUnits() throws Exception {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    final Document cart = page("/cart");

    assertThat(cart.select(".cart__summary-row span").eachText())
        .containsExactly("Total Items: 2", "Total Quantity: 3");
  }

  @Test
  @DisplayName("Below the lines the cart shows the subtotal")
  void cartShowsTheSubtotalBelowTheLines() throws Exception {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    final Document cart = page("/cart");

    assertThat(totalRow(cart, "Subtotal")).isEqualTo("149.97 EUR");
  }

  @Test
  @DisplayName("Below the subtotal the cart shows the VAT it contains")
  void cartShowsTheVatItsSubtotalContains() throws Exception {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    final Document cart = page("/cart");

    assertThat(totalRow(cart, "incl. VAT")).isEqualTo("23.94 EUR");
  }

  @Test
  @DisplayName("The VAT the cart shows is rounded half up to the cent")
  void containedVatIsRoundedHalfUpToTheCent() throws Exception {
    addToCart("Domain-Driven Design");

    final Document cart = page("/cart");

    assertThat(totalRow(cart, "Subtotal")).isEqualTo("54.99 EUR");
    assertThat(totalRow(cart, "incl. VAT")).isEqualTo("8.78 EUR");
  }

  @Test
  @DisplayName("An empty cart invites the visitor to browse")
  void emptyCartInvitesTheVisitorToBrowse() throws Exception {
    final Document cart = page("/cart");

    assertThat(cart.selectFirst(".empty-state__title").text()).isEqualTo("Your cart is empty");
    assertThat(cart.selectFirst(".empty-state__message").text())
        .isEqualTo("Browse our products to find something you'll love.");
    final Element browse = cart.selectFirst("[data-test=cart-browse-link]");
    assertThat(browse.text()).isEqualTo("Browse Products");
    assertThat(browse.attr("href")).isEqualTo("/products");
    assertThat(cart.select("[data-test=cart-item], .cart__summary, .cart__total")).isEmpty();
  }

  /** The amount a row below the cart's lines shows beside its label. */
  private static String totalRow(final Document cart, final String label) {
    return cart.select(".cart__total-row").stream()
        .filter(row -> label.equals(row.child(0).text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no total row " + label))
        .child(1)
        .text();
  }

  /** Presses "Add to Cart" on the product page of the product with that name. */
  private void addToCart(final String productName) throws Exception {
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
