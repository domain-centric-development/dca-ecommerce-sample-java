package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
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
 * Starting the checkout again from the cart, rendered by the wired application — the visitor's
 * cookies carried from request to request, as a browser would: the open checkout is replaced by one
 * over the cart as it is now.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:restart_the_checkout_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Start the checkout again")
class RestartTheCheckoutIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String PIN = "\"Bounded Context\" Enamel Pin";
  private static final String HOODIE = "\"Domain over Framework\" Hoodie";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void aNewVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Restart takes the current cart")
  void startingAgainShowsTheCartAsItIsNowWithItsSubtotal() throws Exception {
    addToCart(PIN, 2);
    proceedToCheckout();
    addToCart(PIN, 3);
    addToCart(HOODIE, 1);

    proceedToCheckout();

    final Document buyer = page(BUYER_STEP);
    assertThat(
            buyer.select("[data-test=order-summary-item]").stream()
                .map(
                    item ->
                        item.selectFirst("[data-test=order-summary-item-name]").text()
                            + " "
                            + item.selectFirst("[data-test=order-summary-item-qty]").text())
                .toList())
        .containsExactly(PIN + " x5", HOODIE + " x1");
    assertThat(buyer.select(".order-summary__total span").eachText())
        .containsExactly("Subtotal", "124.94 EUR");
  }

  @Test
  @DisplayName("Restart begins with empty forms")
  void startingAgainAfterTheBuyerStepOpensItWithAllFieldsEmpty() throws Exception {
    addToCart(PIN, 1);
    proceedToCheckout();
    final MvcResult submitted =
        submit(
            page(BUYER_STEP).selectFirst("[data-test=buyer-form]"),
            Map.of(
                "email", "jane@example.com",
                "firstName", "Jane",
                "lastName", "Doe",
                "phone", "+49 30 1234567"));
    assertThat(URI.create(submitted.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo("/checkout/delivery");
    page("/cart");

    proceedToCheckout();

    final Element form = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    assertThat(
            form
                .select(
                    "[data-test=buyer-email-input], [data-test=buyer-firstname-input],"
                        + " [data-test=buyer-lastname-input], [data-test=buyer-phone-input]")
                .stream()
                .map(input -> input.attr("name") + "=" + input.val())
                .toList())
        .containsExactly("email=", "firstName=", "lastName=", "phone=");
  }

  /** Presses "Proceed to Checkout" on the cart page; the shop sends the guest to the buyer step. */
  private void proceedToCheckout() throws Exception {
    final MvcResult started =
        submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"), Map.of());
    assertThat(URI.create(started.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(BUYER_STEP);
  }

  /** Presses "Add to Cart" on the product page of the product with that name, that many times. */
  private void addToCart(final String productName, final int times) throws Exception {
    for (int i = 0; i < times; i++) {
      final Element card =
          page("/products").select("[data-test=product-card]").stream()
              .filter(
                  c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no card titled " + productName));
      final Document productPage = page(card.selectFirst("a[data-test=view-product]").attr("href"));
      submit(productPage.selectFirst("[data-test=product-add-to-cart-form]"), Map.of());
    }
  }

  /**
   * Submits a form with the values its fields carry, the given ones in place, as a browser would.
   */
  private MvcResult submit(final Element form, final Map<String, String> entered) throws Exception {
    final Map<String, String> values = new LinkedHashMap<>();
    for (final Element field : form.select("input[name]")) {
      values.put(field.attr("name"), field.val());
    }
    values.putAll(entered);
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    values.forEach(request::param);
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
    return result;
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
