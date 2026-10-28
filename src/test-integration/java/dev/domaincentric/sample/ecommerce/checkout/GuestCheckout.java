package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * A guest walking the checkout pages of the wired application, the visitor's cookies carried from
 * request to request, as a browser would.
 */
final class GuestCheckout {

  static final String REVIEW_STEP = "/checkout/review";

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";

  private final MockMvc mockMvc;
  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  GuestCheckout(final MockMvc mockMvc) {
    this.mockMvc = mockMvc;
  }

  /**
   * Puts "Domain-Driven Design" into the cart once, starts the checkout, enters the buyer, the
   * delivery with "Standard Shipping" and "Mock Payment (Test)", and opens the review.
   *
   * @return the review page
   */
  Document toTheReview() throws Exception {
    return toTheReviewAs("jane@example.com");
  }

  /**
   * The same way to the review as {@link #toTheReview()}, with that buyer e-mail.
   *
   * @param buyerEmail the e-mail the guest enters at the buyer step
   * @return the review page
   */
  Document toTheReviewAs(final String buyerEmail) throws Exception {
    addToCart("Domain-Driven Design");
    startTheCheckout();
    enterTheBuyer(buyerEmail);
    enterTheDelivery("Standard Shipping");
    choosePayment("Mock Payment (Test)");
    return page(REVIEW_STEP);
  }

  /**
   * Presses "Place Order" on the review page that is open now; lands on the confirmation page.
   *
   * @return the confirmation page
   */
  Document placeTheOrder() throws Exception {
    final MvcResult placed =
        submit(page(REVIEW_STEP).selectFirst("[data-test=review-confirm-form]"));
    assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo("/checkout/confirmation");
    return page("/checkout/confirmation");
  }

  /** Presses "Checkout" in the cart; lands on the buyer step. */
  private void startTheCheckout() throws Exception {
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);
  }

  /** Enters Jane Doe, with that e-mail, as the buyer; lands on the delivery step. */
  private void enterTheBuyer(final String email) throws Exception {
    final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val(email);
    buyerForm.selectFirst("input[name=firstName]").val("Jane");
    buyerForm.selectFirst("input[name=lastName]").val("Doe");
    buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
    assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);
  }

  /** Enters a Berlin address and the shipping method with that name; lands on the payment step. */
  private void enterTheDelivery(final String shippingMethod) throws Exception {
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
        optionNamed(deliveryForm, "[data-test=delivery-shipping-option]", shippingMethod);
    for (final Element field : method.select("input[name]")) {
      delivery.param(field.attr("name"), field.val());
    }
    assertThat(perform(delivery).getResponse().getRedirectedUrl()).isEqualTo(PAYMENT_STEP);
  }

  /** Chooses the payment provider with that name; lands on the review. */
  private void choosePayment(final String providerName) throws Exception {
    final Element paymentForm = page(PAYMENT_STEP).selectFirst("[data-test=payment-form]");
    final MockHttpServletRequestBuilder payment =
        MockMvcRequestBuilders.post(paymentForm.attr("action"));
    for (final Element field : paymentForm.select("input[name]:not([type=radio])")) {
      payment.param(field.attr("name"), field.val());
    }
    final Element provider =
        optionNamed(paymentForm, "[data-test=payment-provider-option]", providerName);
    payment.param("providerId", provider.selectFirst("input[name=providerId]").val());
    assertThat(perform(payment).getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);
  }

  /** The option card of a form whose label is that name. */
  private static Element optionNamed(
      final Element form, final String cardSelector, final String name) {
    return form.select(cardSelector).stream()
        .filter(o -> name.equals(o.selectFirst(".option-card__label").text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no option named " + name));
  }

  /** Opens a page and answers it parsed; fails unless it answers 200. */
  Document page(final String path) throws Exception {
    final MvcResult result = perform(get(path));
    assertThat(result.getResponse().getStatus()).as("GET " + path).isEqualTo(200);
    return Jsoup.parse(result.getResponse().getContentAsString());
  }

  /** Each item of an order summary as "name xQuantity price". */
  static List<String> itemsOf(final Element container) {
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

  /** Each summary line below the items as "label value". */
  static List<String> totalsOf(final Element container) {
    return container.select(".order-summary__line, .order-summary__total").stream()
        .map(line -> String.join(" ", line.select("> span").eachText()))
        .toList();
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

  /** Submits a form with the values its fields carry, as the browser would; expects a redirect. */
  MvcResult submit(final Element form) throws Exception {
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    final MvcResult result = perform(request);
    assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
    return result;
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
