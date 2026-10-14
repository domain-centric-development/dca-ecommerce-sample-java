package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
 * Starting a checkout from a cart id the visitor sends, in the wired application — two visitors,
 * each with the cookies of their own browser.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:strangers_cart_checkout_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("A visitor never reaches another visitor's checkout")
class StrangersCartCheckoutIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String CART = "/cart";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Starting from a strangers cart is refused")
  void startingTheCheckoutWithTheOwnersCartIdIsRefusedAndLeavesTheOwnersCheckout()
      throws Exception {
    final Visitor owner = new Visitor();
    final String ownersCartId = owner.startsTheCheckoutWithDomainDrivenDesign();
    final Visitor stranger = new Visitor();
    stranger.addToCart("Clean Architecture");
    final Element strangersForm = stranger.page(CART).selectFirst("[data-test=cart-checkout-form]");
    strangersForm.selectFirst("input[name=cartId]").val(ownersCartId);

    final MvcResult refused = stranger.submit(strangersForm);

    assertThat(URI.create(refused.getResponse().getRedirectedUrl()).getPath()).isEqualTo(CART);
    assertThat(errorOn(stranger.followed(refused))).isEqualTo("Your cart could not be found.");
    final Document ownersBuyerStep = owner.page(BUYER_STEP);
    assertThat(summaryOf(ownersBuyerStep)).containsExactly("Domain-Driven Design x1");
    assertThat(ownersBuyerStep.selectFirst("[data-test=buyer-email-input]").val())
        .isEqualTo("jane@example.com");
  }

  /** The cart page's error message, or null when it shows none. */
  private static String errorOn(final Document page) {
    final Element error = page.selectFirst("[data-test=cart-error-message]");
    return error == null ? null : error.text();
  }

  /** Each order summary line as "name xQuantity". */
  private static List<String> summaryOf(final Document page) {
    return page.select("[data-test=order-summary-item]").stream()
        .map(
            item ->
                item.selectFirst("[data-test=order-summary-item-name]").text()
                    + " "
                    + item.selectFirst("[data-test=order-summary-item-qty]").text())
        .toList();
  }

  /** One browser: its cookies carried from request to request. */
  private final class Visitor {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /**
     * Puts "Domain-Driven Design" into the cart once, starts the checkout and submits the buyer
     * step with "jane@example.com".
     *
     * @return the id of the cart the checkout was started from
     */
    String startsTheCheckoutWithDomainDrivenDesign() throws Exception {
      addToCart("Domain-Driven Design");
      final Element checkoutForm = page(CART).selectFirst("[data-test=cart-checkout-form]");
      final String cartId = checkoutForm.selectFirst("input[name=cartId]").val();
      assertThat(submit(checkoutForm).getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);

      final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
      buyerForm.selectFirst("input[name=email]").val("jane@example.com");
      buyerForm.selectFirst("input[name=firstName]").val("Jane");
      buyerForm.selectFirst("input[name=lastName]").val("Doe");
      buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
      assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);
      return cartId;
    }

    /** Presses "Add to Cart" on the product page of the product with that name. */
    void addToCart(final String productName) throws Exception {
      final Element card =
          page("/products").select("[data-test=product-card]").stream()
              .filter(
                  c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no card titled " + productName));
      final Document productPage = page(card.selectFirst("a[data-test=view-product]").attr("href"));
      submit(productPage.selectFirst("[data-test=product-add-to-cart-form]"));
    }

    /** Submits a form with the values its fields carry, as the browser would. */
    MvcResult submit(final Element form) throws Exception {
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(form.attr("action"));
      for (final Element field : form.select("input[name]")) {
        request.param(field.attr("name"), field.val());
      }
      final MvcResult result = perform(request);
      assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
      return result;
    }

    /** Follows a redirect with the messages it carries, as the browser would. */
    Document followed(final MvcResult redirect) throws Exception {
      final String target = redirect.getResponse().getRedirectedUrl();
      final MvcResult result = perform(get(URI.create(target)));
      assertThat(result.getResponse().getStatus()).as("GET " + target).isEqualTo(200);
      return Jsoup.parse(result.getResponse().getContentAsString());
    }

    Document page(final String path) throws Exception {
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
}
