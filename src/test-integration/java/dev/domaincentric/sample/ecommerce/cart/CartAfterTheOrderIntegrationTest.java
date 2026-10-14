package dev.domaincentric.sample.ecommerce.cart;

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
 * The cart page after a guest placed an order, rendered by the wired application — the visitor's
 * cookies carried from request to request, as a browser would. The order reaches the cart as an
 * event after it is placed, so the cart page is read again until it changed, for up to 15 seconds.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_after_the_order_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("After the order the cart keeps only what was not bought")
class CartAfterTheOrderIntegrationTest {

  private static final String PIN = "\"Bounded Context\" Enamel Pin";
  private static final String HOODIE = "\"Domain over Framework\" Hoodie";
  private static final String T_SHIRT = "\"Ports & Adapters\" T-Shirt";

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";
  private static final long RECONCILED_WITHIN_MILLIS = 15_000;

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  /** The cart lines the page showed when the guest set out to place the order. */
  private List<String> linesBeforeTheOrder = List.of();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
    linesBeforeTheOrder = List.of();
  }

  @Test
  @DisplayName("A bought line leaves the cart completely")
  void aLineTheOrderBoughtWhollyLeavesTheCartEmpty() throws Exception {
    addToCart(PIN, 3);
    startTheCheckout();

    placeTheOrderFromTheReview();

    final Document cart = cartOnceTheOrderReachedIt();
    assertThat(cart.select(".empty-state__title").eachText()).containsExactly("Your cart is empty");
  }

  @Test
  @DisplayName("Units added after the start stay")
  void unitsAddedAfterTheCheckoutStartedStayInTheCart() throws Exception {
    addToCart(PIN, 3);
    startTheCheckout();
    addToCart(PIN, 2);
    assertThat(linesOf(page("/cart"))).containsExactly(PIN + " x5");

    placeTheOrderFromTheReview();

    assertThat(linesOf(cartOnceTheOrderReachedIt())).containsExactly(PIN + " x2");
  }

  @Test
  @DisplayName("The cart accepts new items again")
  void theCartTakesANewItemAfterTheOrderAndOffersTheCheckout() throws Exception {
    addToCart(PIN, 2);
    addToCart(HOODIE, 1);
    startTheCheckout();
    addToCart(T_SHIRT, 1);
    placeTheOrderFromTheReview();
    assertThat(linesOf(cartOnceTheOrderReachedIt())).containsExactly(T_SHIRT + " x1");

    addToCart(HOODIE, 1);

    final Document cart = page("/cart");
    assertThat(linesOf(cart)).containsExactly(T_SHIRT + " x1", HOODIE + " x1");
    assertThat(cart.selectFirst("[data-test=cart-checkout-button]").text())
        .isEqualTo("Proceed to Checkout");
  }

  /** Each cart line as "name xQuantity", in the order the page shows them. */
  private static List<String> linesOf(final Document cart) {
    return cart.select("[data-test=cart-item]").stream()
        .map(
            line ->
                line.selectFirst("[data-test=cart-item-name]").text()
                    + " x"
                    + line.selectFirst("[data-test=cart-item-quantity]").text())
        .toList();
  }

  /** The cart page once it shows something else than before the order, or after 15 seconds. */
  private Document cartOnceTheOrderReachedIt() throws Exception {
    final long deadline = System.currentTimeMillis() + RECONCILED_WITHIN_MILLIS;
    Document cart = page("/cart");
    while (linesOf(cart).equals(linesBeforeTheOrder) && System.currentTimeMillis() < deadline) {
      Thread.sleep(50);
      cart = page("/cart");
    }
    return cart;
  }

  /** Presses "Proceed to Checkout" on the cart page; the shop sends the guest to the buyer step. */
  private void startTheCheckout() throws Exception {
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);
  }

  /**
   * The guest submits the buyer step, the delivery step with "Standard Shipping", chooses "Mock
   * Payment (Test)" and presses "Place Order" on the review, landing on the confirmation.
   */
  private void placeTheOrderFromTheReview() throws Exception {
    linesBeforeTheOrder = linesOf(page("/cart"));

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
  }

  /** Presses "Add to Cart" on the product page of the product with that name, that many times. */
  private void addToCart(final String productName, final int times) throws Exception {
    final Element card =
        page("/products").select("[data-test=product-card]").stream()
            .filter(c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled " + productName));
    final String productPath = card.selectFirst("a[data-test=view-product]").attr("href");
    for (int i = 0; i < times; i++) {
      submit(page(productPath).selectFirst("[data-test=product-add-to-cart-form]"));
    }
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
