package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.util.ArrayList;
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
 * The review step and the confirmation page, rendered by the wired application as it runs without a
 * provider address — the visitor's cookies carried from request to request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:place_the_order_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Place the order")
class PlaceTheOrderIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Review shows buyer, delivery and payment")
  void theReviewShowsTheBuyerTheDeliveryAndTheChosenProvider() throws Exception {
    final Document review = reviewStep();

    final Element buyer = review.selectFirst("[data-test=review-buyer-section]");
    assertThat(buyer.selectFirst(".review-section__title").text()).isEqualTo("Buyer Information");
    assertThat(buyer.select(".review-section__body > p").eachText())
        .containsExactly("Jane Doe", "jane@example.com", "+49 30 1234567");

    final Element delivery = review.selectFirst("[data-test=review-delivery-section]");
    assertThat(delivery.selectFirst(".review-section__title").text()).isEqualTo("Delivery");
    assertThat(delivery.select(".review-section__body > *").eachText())
        .containsExactly(
            "Shipping Address",
            "Hauptstrasse 1",
            "Berlin, 10115",
            "Germany",
            "Shipping Method",
            "Standard Shipping",
            "Estimated Delivery: 5-7 business days",
            "Cost: 4.99 EUR");

    final Element payment = review.selectFirst("[data-test=review-payment-section]");
    assertThat(payment.selectFirst(".review-section__title").text()).isEqualTo("Payment Method");
    assertThat(payment.selectFirst(".review-section__body > p").text()).isEqualTo("mock");
  }

  @Test
  @DisplayName("Review shows items and totals")
  void theReviewShowsTheItemsAndTheTotalsWithTheContainedTax() throws Exception {
    final Document review = reviewStep();

    final Element summary = review.selectFirst(".order-summary");
    assertThat(summary.selectFirst(".order-summary__title").text()).isEqualTo("Order Items");
    assertThat(itemsOf(summary)).containsExactly("Domain-Driven Design x1 54.99 EUR");
    assertThat(totalsOf(summary))
        .containsExactly(
            "Subtotal 54.99 EUR", "Shipping 4.99 EUR", "Tax 9.58 EUR", "Total 59.98 EUR");
    assertThat(review.selectFirst("[data-test=review-place-order-button]").text())
        .isEqualTo("Place Order");
  }

  @Test
  @DisplayName("Confirmation shows what was ordered")
  void theConfirmationShowsWhatWasOrdered() throws Exception {
    final Document confirmation = placeTheOrder();

    assertThat(paragraphsUnder(confirmation, "Contact Information"))
        .containsExactly("Jane Doe", "jane@example.com", "+49 30 1234567");
    assertThat(paragraphsUnder(confirmation, "Delivery Address"))
        .containsExactly("Hauptstrasse 1", "Berlin, 10115", "Germany");
    assertThat(paragraphsUnder(confirmation, "Shipping Method"))
        .containsExactly("Standard Shipping", "Estimated Delivery: 5-7 business days");
    assertThat(paragraphsUnder(confirmation, "Payment Method")).containsExactly("mock");
    assertThat(itemsOf(headingNamed(confirmation, "Items Ordered").parent()))
        .containsExactly("Domain-Driven Design x1 54.99 EUR");
    assertThat(totalsOf(confirmation.selectFirst(".confirmation__details")))
        .contains("Subtotal 54.99 EUR", "Shipping 4.99 EUR", "Tax 9.58 EUR");
  }

  @Test
  @DisplayName("Continue shopping leads home")
  void continueShoppingOnTheConfirmationOpensTheHomePage() throws Exception {
    final Element link = placeTheOrder().selectFirst("[data-test=confirmation-continue-link]");
    assertThat(link.text()).isEqualTo("Continue Shopping");
    assertThat(link.attr("href")).isEqualTo("/");

    assertThat(perform(get(link.attr("href"))).getResponse().getStatus()).isEqualTo(200);
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

  /** Each summary line below the items as "label value". */
  private static List<String> totalsOf(final Element container) {
    return container.select(".order-summary__line, .order-summary__total").stream()
        .map(line -> String.join(" ", line.select("> span").eachText()))
        .toList();
  }

  /** The paragraphs that follow a heading, up to the next heading. */
  private static List<String> paragraphsUnder(final Document page, final String heading) {
    final List<String> paragraphs = new ArrayList<>();
    for (Element next = headingNamed(page, heading).nextElementSibling();
        next != null && "p".equals(next.tagName());
        next = next.nextElementSibling()) {
      paragraphs.add(next.text());
    }
    return paragraphs;
  }

  private static Element headingNamed(final Document page, final String heading) {
    return page.select("h4").stream()
        .filter(h -> heading.equals(h.text()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no heading " + heading));
  }

  /** The guest presses "Place Order" on the review step and lands on the confirmation page. */
  private Document placeTheOrder() throws Exception {
    final MvcResult placed = submit(reviewStep().selectFirst("[data-test=review-confirm-form]"));
    assertThat(URI.create(placed.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(CONFIRMATION);
    return page(CONFIRMATION);
  }

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, submitted the buyer step,
   * the delivery step with "Standard Shipping", chose "Mock Payment (Test)" and opened the review.
   */
  private Document reviewStep() throws Exception {
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
    assertThat(URI.create(perform(delivery).getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(PAYMENT_STEP);

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
    assertThat(URI.create(perform(payment).getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(REVIEW_STEP);

    return page(REVIEW_STEP);
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
