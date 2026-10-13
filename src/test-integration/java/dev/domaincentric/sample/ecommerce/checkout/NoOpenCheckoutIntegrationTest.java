package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.List;
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
 * The step pages and the confirmation for a visitor without an open checkout, rendered by the wired
 * application — the visitor's cookies carried from request to request, and a redirect's message
 * carried to the page it leads to, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:no_open_checkout_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Without an open checkout")
class NoOpenCheckoutIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";
  private static final String CART = "/cart";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Later step without a checkout leads to the cart")
  void theReviewWithoutACheckoutLeadsToTheCartWithAMessage() throws Exception {
    final MvcResult review = perform(get(REVIEW_STEP));

    assertThat(review.getResponse().getRedirectedUrl()).isEqualTo(CART);
    assertThat(errorOn(followed(review))).isEqualTo("No active checkout session found");
  }

  @Test
  @DisplayName("Steps after the order lead to the cart")
  void theBuyerStepAfterTheOrderLeadsToTheCartWithAMessage() throws Exception {
    placeTheOrder();

    final MvcResult buyer = perform(get(BUYER_STEP));

    assertThat(buyer.getResponse().getRedirectedUrl()).isEqualTo(CART);
    assertThat(errorOn(followed(buyer))).isEqualTo("No active checkout session found");
  }

  @Test
  @DisplayName("Confirmation can be reopened")
  void theConfirmationCanBeReopenedAfterBrowsingTheCatalogue() throws Exception {
    placeTheOrder();
    page("/products");

    final Document confirmation = page(CONFIRMATION);

    assertThat(confirmation.selectFirst("[data-test=confirmation-title]").text())
        .isEqualTo("Your order has been placed successfully");
    assertThat(itemsOf(headingNamed(confirmation, "Items Ordered").parent()))
        .containsExactly("Domain-Driven Design x1 54.99 EUR");
  }

  @Test
  @DisplayName("Confirmation without an order leads to the cart")
  void theConfirmationWithoutAnOrderLeadsToTheCartWithAMessage() throws Exception {
    final MvcResult confirmation = perform(get(CONFIRMATION));

    assertThat(confirmation.getResponse().getRedirectedUrl()).isEqualTo(CART);
    assertThat(errorOn(followed(confirmation))).isEqualTo("No confirmed order found");
  }

  /** The cart page's error message, or null when it shows none. */
  private static String errorOn(final Document page) {
    final Element error = page.selectFirst("[data-test=cart-error-message]");
    return error == null ? null : error.text();
  }

  /** Each item as "name xQuantity price". */
  private static List<String> itemsOf(final Element container) {
    return container.select(".order-summary__item").stream()
        .map(
            item ->
                item.selectFirst(".order-summary__item-name").text()
                    + " "
                    + item.selectFirst(".order-summary__item-qty").text()
                    + " "
                    + item.selectFirst(".order-summary__item-price").text())
        .toList();
  }

  private static Element headingNamed(final Document page, final String heading) {
    return page.select("h4").stream()
        .filter(h -> heading.equals(h.text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no heading " + heading));
  }

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, went through every step with
   * "Standard Shipping" and "Mock Payment (Test)", and pressed "Place Order" on the review.
   */
  private void placeTheOrder() throws Exception {
    addToCart("Domain-Driven Design");
    final MvcResult started = submit(page(CART).selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);

    final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val("jane@example.com");
    buyerForm.selectFirst("input[name=firstName]").val("Jane");
    buyerForm.selectFirst("input[name=lastName]").val("Doe");
    buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
    assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);

    final Element deliveryForm = page(DELIVERY_STEP).selectFirst("[data-test=delivery-form]");
    deliveryForm.selectFirst("input[name=street]").val("Hauptstrasse 1");
    deliveryForm.selectFirst("input[name=city]").val("Berlin");
    deliveryForm.selectFirst("input[name=postalCode]").val("10115");
    deliveryForm.selectFirst("input[name=country]").val("Germany");
    final MockHttpServletRequestBuilder delivery =
        MockMvcRequestBuilders.post(deliveryForm.attr("action"));
    for (final Element field :
        deliveryForm.select("input[name]:not([disabled]):not([type=radio])")) {
      delivery.param(field.attr("name"), field.val());
    }
    final Element method =
        deliveryForm.select("[data-test=delivery-shipping-option]").stream()
            .filter(o -> "Standard Shipping".equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no shipping method named Standard Shipping"));
    for (final Element field : method.select("input[name]")) {
      delivery.param(field.attr("name"), field.val());
    }
    assertThat(perform(delivery).getResponse().getRedirectedUrl()).isEqualTo(PAYMENT_STEP);

    final Element paymentForm = page(PAYMENT_STEP).selectFirst("[data-test=payment-form]");
    final MockHttpServletRequestBuilder payment =
        MockMvcRequestBuilders.post(paymentForm.attr("action"));
    for (final Element field : paymentForm.select("input[name]:not([type=radio])")) {
      payment.param(field.attr("name"), field.val());
    }
    final Element provider =
        paymentForm.select("[data-test=payment-provider-option]").stream()
            .filter(o -> "Mock Payment (Test)".equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no provider named Mock Payment (Test)"));
    payment.param("providerId", provider.selectFirst("input[name=providerId]").val());
    assertThat(perform(payment).getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);

    final MvcResult placed =
        submit(page(REVIEW_STEP).selectFirst("[data-test=review-confirm-form]"));
    assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo(CONFIRMATION);
    page(CONFIRMATION);
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
  private MvcResult submit(final Element form) throws Exception {
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
    return result;
  }

  /** Follows a redirect with the messages it carries, as the browser would. */
  private Document followed(final MvcResult redirect) throws Exception {
    final String target = redirect.getResponse().getRedirectedUrl();
    final MvcResult result = perform(get(target).flashAttrs(redirect.getFlashMap()));
    assertThat(result.getResponse().getStatus()).as("GET " + target).isEqualTo(200);
    return Jsoup.parse(result.getResponse().getContentAsString());
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
