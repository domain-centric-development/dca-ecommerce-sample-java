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
 * The payment step of the checkout, rendered by the wired application as it runs without a provider
 * address — the visitor's cookies carried from request to request, as a browser would, and the
 * payment form sent without the browser's own checks.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:choose_how_to_pay_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Choose how to pay")
class ChooseHowToPayIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Payment step offers the provider")
  void thePaymentStepOffersMockPaymentWithABackLinkAndAButton() throws Exception {
    final Document payment = paymentStepAfterChoosing("Standard Shipping");

    final Element form = payment.selectFirst("[data-test=payment-form]");
    final Element options = form.selectFirst("[data-test=payment-options]");
    assertThat(options.previousElementSibling().text()).isEqualTo("Select Payment Method");
    assertThat(
            options.select("[data-test=payment-provider-option]").stream()
                .map(option -> option.selectFirst(".option-card__label").text())
                .toList())
        .containsExactly("Mock Payment (Test)");
    assertThat(payment.selectFirst("[data-test=payment-back-link]").text()).isEqualTo("Back");
    assertThat(payment.selectFirst("[data-test=payment-continue-button]").text())
        .isEqualTo("Continue to Review");
  }

  @Test
  @DisplayName("Payment step totals with standard shipping")
  void standardShippingIsAddedToTheOrderTotal() throws Exception {
    final Document payment = paymentStepAfterChoosing("Standard Shipping");

    final Element summary = payment.selectFirst(".order-summary");
    assertThat(summary.selectFirst(".order-summary__title").text()).isEqualTo("Order Summary");
    assertThat(
            summary.select("[data-test=order-summary-item]").stream()
                .map(
                    item ->
                        item.selectFirst("[data-test=order-summary-item-name]").text()
                            + " "
                            + item.selectFirst("[data-test=order-summary-item-qty]").text()
                            + " "
                            + item.selectFirst(".order-summary__item-price").text())
                .toList())
        .containsExactly("Domain-Driven Design x1 54.99 EUR");
    assertThat(totalsOf(summary))
        .containsExactly("Subtotal 54.99 EUR", "Shipping 4.99 EUR", "Total 59.98 EUR");
  }

  @Test
  @DisplayName("Payment step totals with express shipping")
  void expressShippingIsAddedToTheOrderTotal() throws Exception {
    final Document payment = paymentStepAfterChoosing("Express Shipping");

    assertThat(totalsOf(payment.selectFirst(".order-summary")))
        .containsExactly("Subtotal 54.99 EUR", "Shipping 9.99 EUR", "Total 64.98 EUR");
  }

  @Test
  @DisplayName("Unknown provider is refused")
  void aProviderTheShopDoesNotOfferOpensThePaymentStepAgainWithTheRefusal() throws Exception {
    final Element form =
        paymentStepAfterChoosing("Standard Shipping").selectFirst("[data-test=payment-form]");
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]:not([type=radio])")) {
      request.param(field.attr("name"), field.val());
    }
    request.param("providerId", "paypal");
    final MvcResult posted = perform(request);
    assertThat(URI.create(posted.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(PAYMENT_STEP);

    final Document landed = follow(posted, PAYMENT_STEP);

    assertThat(landed.selectFirst("[data-test=payment-error-message]").text())
        .isEqualTo("This way to pay is not offered. Please choose another one.");
  }

  /** Each summary line below the items as "label value": Subtotal, Shipping, Total. */
  private static List<String> totalsOf(final Element summary) {
    return summary.select(".order-summary__line, .order-summary__total").stream()
        .map(line -> String.join(" ", line.select("> span").eachText()))
        .toList();
  }

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, submitted the buyer step and
   * the delivery step with the given shipping method, and opened the payment step.
   */
  private Document paymentStepAfterChoosing(final String shippingMethod) throws Exception {
    addToCart("Domain-Driven Design");
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(URI.create(started.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(BUYER_STEP);

    final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val("jane@example.com");
    buyerForm.selectFirst("input[name=firstName]").val("Jane");
    buyerForm.selectFirst("input[name=lastName]").val("Doe");
    buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
    assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);

    final Element deliveryForm = page(DELIVERY_STEP).selectFirst("[data-test=delivery-form]");
    final MockHttpServletRequestBuilder delivery =
        MockMvcRequestBuilders.post(deliveryForm.attr("action"));
    for (final Element field :
        deliveryForm.select("input[name]:not([disabled]):not([type=radio])")) {
      delivery.param(field.attr("name"), field.val());
    }
    delivery.param("street", "Hauptstrasse 1");
    delivery.param("city", "Berlin");
    delivery.param("postalCode", "10115");
    delivery.param("country", "Germany");
    final Element method =
        deliveryForm.select("[data-test=delivery-shipping-option]").stream()
            .filter(o -> shippingMethod.equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no shipping method named " + shippingMethod));
    for (final Element field : method.select("input[name]")) {
      delivery.param(field.attr("name"), field.val());
    }
    assertThat(URI.create(perform(delivery).getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(PAYMENT_STEP);

    return page(PAYMENT_STEP);
  }

  /** Follows a redirect with the session that carries the page's message. */
  private Document follow(final MvcResult posted, final String path) throws Exception {
    final MockHttpServletRequestBuilder follow =
        get(URI.create(posted.getResponse().getRedirectedUrl()));
    final MvcResult landed = perform(follow);
    assertThat(landed.getResponse().getStatus()).isEqualTo(200);
    return Jsoup.parse(landed.getResponse().getContentAsString());
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
