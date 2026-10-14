package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock.ProvisionInitialStockInputPort;
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
 * The stock warnings on the cart page, rendered by the wired application after Inventory's stock
 * level for a product in the cart was lowered — as another visitor's order would lower it.
 */
// A context of its own: the stock taken away here must not reach the classes asserting the seeded
// stock.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_stock_warnings_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Stock warnings in the cart")
class CartStockWarningsIntegrationTest {

  private static final String DESK_MODEL = "Wooden Hexagon Desk Model";
  private static final String STOCK_WARNING = "[data-test=stock-alert]";

  @Autowired private MockMvc mockMvc;

  @Autowired private ProvisionInitialStockInputPort provisionInitialStock;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitorAndRestockTheSeededUnits() throws Exception {
    cookies.clear();
    provisionInitialStock.execute(new ProvisionInitialStockCommand(productIdOf(DESK_MODEL), 8));
  }

  @Test
  @DisplayName("Sold out line says unavailable")
  void aLineWhoseProductSoldOutSaysOnlyThatItIsNoLongerAvailable() throws Exception {
    addToCart(DESK_MODEL);
    provisionInitialStock.execute(new ProvisionInitialStockCommand(productIdOf(DESK_MODEL), 0));

    final Element line = lineOf(page("/cart"), DESK_MODEL);

    assertThat(line.select(STOCK_WARNING).eachText()).contains("Product no longer available");
    assertThat(line.select(STOCK_WARNING).eachText())
        .doesNotContain("Insufficient stock available");
  }

  @Test
  @DisplayName("Only the short line warns")
  void onlyTheLineShortOfStockShowsAWarning() throws Exception {
    addToCart("Team Topologies");
    addToCart(DESK_MODEL);
    addToCart(DESK_MODEL);
    provisionInitialStock.execute(new ProvisionInitialStockCommand(productIdOf(DESK_MODEL), 1));

    final Document cart = page("/cart");

    assertThat(cart.select(STOCK_WARNING)).hasSize(1);
    assertThat(lineOf(cart, DESK_MODEL).select(STOCK_WARNING)).hasSize(1);
  }

  private static Element lineOf(final Document cart, final String productName) {
    return cart.select("[data-test=cart-item]").stream()
        .filter(line -> productName.equals(line.selectFirst("[data-test=cart-item-name]").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no cart line " + productName));
  }

  /** The id the catalogue's link to the product's page carries. */
  private String productIdOf(final String productName) throws Exception {
    return productLink(productName).substring("/products/".length());
  }

  private String productLink(final String productName) throws Exception {
    final Element card =
        page("/products").select("[data-test=product-card]").stream()
            .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled " + productName));
    return card.selectFirst("a[data-test=view-product]").attr("href");
  }

  /** Presses "Add to Cart" on the product page of the product with that name. */
  private void addToCart(final String productName) throws Exception {
    final Document productPage = page(productLink(productName));
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
