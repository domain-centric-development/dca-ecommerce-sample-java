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
 * Going back from a later checkout step to an earlier one and changing it, rendered by the wired
 * application as it runs without a provider address — the visitor's cookies carried from request to
 * request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:go_back_to_an_earlier_step_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Go back to an earlier step and change it")
class GoBackToAnEarlierStepIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Back from payment opens the delivery step")
  void backFromThePaymentStepOpensTheDeliveryStepFilledIn() throws Exception {
    final Document payment = paymentStepAfterStandardShipping();

    final Element back = payment.selectFirst("[data-test=payment-back-link]");
    assertThat(back.text()).isEqualTo("Back");
    final Document delivery = page(back.attr("href"));

    assertThat(back.attr("href")).isEqualTo(DELIVERY_STEP);
    assertThat(addressOf(delivery)).containsExactly("Hauptstrasse 1", "Berlin", "10115", "Germany");
    assertThat(chosenShippingMethodOf(delivery)).isEqualTo("Standard Shipping");
  }

  @Test
  @DisplayName("Back from review opens the payment step")
  void backFromTheReviewOpensThePaymentStepWithTheProviderChosen() throws Exception {
    final Document review = reviewStep();

    final Element back = review.selectFirst("[data-test=review-back-link]");
    assertThat(back.text()).isEqualTo("Back");
    assertThat(back.attr("href")).isEqualTo(PAYMENT_STEP);
    final Document payment = page(back.attr("href"));

    assertThat(chosenProviderOf(payment)).isEqualTo("Mock Payment (Test)");
  }

  @Test
  @DisplayName("Edit links on the review")
  void eachSectionOfTheReviewLinksToItsStep() throws Exception {
    final Document review = reviewStep();

    final Element editDelivery = editLinkOf(review, "review-delivery-section");
    assertThat(editDelivery.attr("href")).isEqualTo(DELIVERY_STEP);
    final Document delivery = page(editDelivery.attr("href"));
    assertThat(addressOf(delivery)).containsExactly("Hauptstrasse 1", "Berlin", "10115", "Germany");
    assertThat(chosenShippingMethodOf(delivery)).isEqualTo("Standard Shipping");

    assertThat(sectionTitleOf(review, "review-buyer-section")).isEqualTo("Buyer Information");
    assertThat(editLinkOf(review, "review-buyer-section").attr("href")).isEqualTo(BUYER_STEP);
    assertThat(sectionTitleOf(review, "review-payment-section")).isEqualTo("Payment Method");
    assertThat(editLinkOf(review, "review-payment-section").attr("href")).isEqualTo(PAYMENT_STEP);
  }

  @Test
  @DisplayName("Changed shipping recalculates the totals")
  void submittingTheDeliveryAgainWithAnotherMethodRecalculatesTheTotals() throws Exception {
    final Document delivery = deliveryStepReopenedFromTheReview();

    final MvcResult resubmitted = submitDelivery(delivery, "Express Shipping");

    assertThat(URI.create(resubmitted.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(PAYMENT_STEP);
    assertThat(totalsOf(page(PAYMENT_STEP).selectFirst(".order-summary")))
        .contains("Shipping 9.99 EUR", "Total 64.98 EUR");
  }

  @Test
  @DisplayName("Changed step keeps the later progress")
  void theReviewStillOpensAfterAnEarlierStepWasChanged() throws Exception {
    submitDelivery(deliveryStepReopenedFromTheReview(), "Express Shipping");

    final Document review = page(REVIEW_STEP);

    final Element delivery = review.selectFirst("[data-test=review-delivery-section]");
    assertThat(delivery.select(".review-section__body > p").eachText())
        .contains("Express Shipping", "Estimated Delivery: 2-3 business days", "Cost: 9.99 EUR");
    assertThat(totalsOf(review.selectFirst(".order-summary")))
        .containsExactly(
            "Subtotal 54.99 EUR", "Shipping 9.99 EUR", "Tax 10.37 EUR", "Total 64.98 EUR");
    final Element payment = review.selectFirst("[data-test=review-payment-section]");
    assertThat(payment.selectFirst(".review-section__body > p").text()).isEqualTo("mock");
  }

  @Test
  @DisplayName("Changed buyer details reach the review")
  void changedBuyerDetailsKeepTheAddressAndReachTheReview() throws Exception {
    final Document review = reviewStep();
    final Element buyerForm =
        page(editLinkOf(review, "review-buyer-section").attr("href"))
            .selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val("jane.doe@example.com");

    final MvcResult resubmitted = submit(buyerForm);

    assertThat(URI.create(resubmitted.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(DELIVERY_STEP);
    assertThat(addressOf(page(DELIVERY_STEP)))
        .containsExactly("Hauptstrasse 1", "Berlin", "10115", "Germany");
    final Element buyer = page(REVIEW_STEP).selectFirst("[data-test=review-buyer-section]");
    assertThat(buyer.select(".review-section__body > p").eachText())
        .contains("jane.doe@example.com");
  }

  /** The delivery form's street, city, postal code and country as they are filled in. */
  private static List<String> addressOf(final Document delivery) {
    final Element form = delivery.selectFirst("[data-test=delivery-form]");
    return List.of("street", "city", "postalCode", "country").stream()
        .map(name -> form.selectFirst("input[name=" + name + "]").val())
        .toList();
  }

  /** The name of the shipping method whose radio is checked. */
  private static String chosenShippingMethodOf(final Document delivery) {
    return delivery.select("[data-test=delivery-shipping-option]").stream()
        .filter(o -> o.selectFirst("[data-test=delivery-shipping-radio]").hasAttr("checked"))
        .map(o -> o.selectFirst(".option-card__label").text())
        .findFirst()
        .orElse("");
  }

  /** The name of the payment provider whose radio is checked. */
  private static String chosenProviderOf(final Document payment) {
    return payment.select("[data-test=payment-provider-option]").stream()
        .filter(o -> o.selectFirst("[data-test=payment-provider-radio]").hasAttr("checked"))
        .map(o -> o.selectFirst(".option-card__label").text())
        .findFirst()
        .orElse("");
  }

  private static String sectionTitleOf(final Document review, final String section) {
    return review.selectFirst("[data-test=" + section + "] .review-section__title").text();
  }

  private static Element editLinkOf(final Document review, final String section) {
    final Element link = review.selectFirst("[data-test=" + section + "] .review-section__edit");
    assertThat(link.text()).isEqualTo("Edit");
    return link;
  }

  /** Each summary line below the items as "label value". */
  private static List<String> totalsOf(final Element container) {
    return container.select(".order-summary__line, .order-summary__total").stream()
        .map(line -> String.join(" ", line.select("> span").eachText()))
        .toList();
  }

  /** A guest on the review step who follows the "Edit" link of its "Delivery" section. */
  private Document deliveryStepReopenedFromTheReview() throws Exception {
    return page(editLinkOf(reviewStep(), "review-delivery-section").attr("href"));
  }

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, submitted the buyer step,
   * the delivery step with "Standard Shipping", chose "Mock Payment (Test)" and opened the review.
   */
  private Document reviewStep() throws Exception {
    final Element paymentForm =
        paymentStepAfterStandardShipping().selectFirst("[data-test=payment-form]");
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

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, submitted the buyer step and
   * the delivery step with "Standard Shipping", and is on the payment step.
   */
  private Document paymentStepAfterStandardShipping() throws Exception {
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

    final Document delivery = page(DELIVERY_STEP);
    final Element deliveryForm = delivery.selectFirst("[data-test=delivery-form]");
    deliveryForm.selectFirst("input[name=street]").val("Hauptstrasse 1");
    deliveryForm.selectFirst("input[name=city]").val("Berlin");
    deliveryForm.selectFirst("input[name=postalCode]").val("10115");
    deliveryForm.selectFirst("input[name=country]").val("Germany");
    assertThat(submitDelivery(delivery, "Standard Shipping").getResponse().getRedirectedUrl())
        .isEqualTo(PAYMENT_STEP);

    return page(PAYMENT_STEP);
  }

  /**
   * Submits the delivery form with the address its fields carry and the named shipping method, as
   * the browser does once the method's radio is checked.
   */
  private MvcResult submitDelivery(final Document delivery, final String shippingMethod)
      throws Exception {
    final Element form = delivery.selectFirst("[data-test=delivery-form]");
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      if (field.closest("[data-test=delivery-shipping-option]") == null) {
        request.param(field.attr("name"), field.val());
      }
    }
    final Element method =
        form.select("[data-test=delivery-shipping-option]").stream()
            .filter(o -> shippingMethod.equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no shipping method named " + shippingMethod));
    for (final Element field : method.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    return perform(request);
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
