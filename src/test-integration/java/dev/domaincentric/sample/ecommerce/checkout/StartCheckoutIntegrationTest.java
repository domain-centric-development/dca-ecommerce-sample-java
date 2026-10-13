package dev.domaincentric.sample.ecommerce.checkout;

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
 * Starting the checkout from the cart page, rendered by the wired application — the visitor's
 * cookies carried from request to request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:start_checkout_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Start the checkout")
class StartCheckoutIntegrationTest {

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("The buyer step summarises the cart at today's catalogue price")
  void buyerStepSummarisesEachCartLineAndTheSubtotal() throws Exception {
    addToCart("Domain-Driven Design");
    addToCart("Domain-Driven Design");
    addToCart("Clean Architecture");

    submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    final Document buyer = page("/checkout/buyer");

    assertThat(buyer.selectFirst(".order-summary__title").text()).isEqualTo("Order Summary");
    assertThat(buyer.select("[data-test=order-summary-item]"))
        .allSatisfy(
            entry ->
                assertThat(entry.select(".order-summary__item-image img[src]").attr("src"))
                    .isNotBlank());
    assertThat(
            buyer.select("[data-test=order-summary-item]").stream()
                .map(
                    entry ->
                        entry.selectFirst("[data-test=order-summary-item-name]").text()
                            + " "
                            + entry.selectFirst("[data-test=order-summary-item-qty]").text()
                            + " "
                            + entry.selectFirst(".order-summary__item-price").text())
                .toList())
        .containsExactlyInAnyOrder(
            "Domain-Driven Design x2 109.98 EUR", "Clean Architecture x1 39.99 EUR");
    final Element subtotal = buyer.selectFirst(".order-summary__total");
    assertThat(subtotal.child(0).text()).isEqualTo("Subtotal");
    assertThat(subtotal.child(1).text()).isEqualTo("149.97 EUR");
  }

  /** Presses "Add to Cart" on the product page of the product with that name. */
  private void addToCart(final String productName) throws Exception {
    final Element card =
        page("/products").select("[data-test=product-card]").stream()
            .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled " + productName));
    final Document productPage = page(card.selectFirst("a[data-test=view-product]").attr("href"));
    submit(productPage.selectFirst("[data-test=product-add-to-cart-form]"));
  }

  /** Submits a form with the values its fields carry, as the browser would. */
  private void submit(final Element form) throws Exception {
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
