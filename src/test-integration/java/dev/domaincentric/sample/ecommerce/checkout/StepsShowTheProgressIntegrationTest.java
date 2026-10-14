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
 * The checkout's progress bar and the order of its steps, rendered by the wired application — the
 * visitor's cookies carried from request to request, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:steps_show_the_progress_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("The steps show the progress and are taken in order")
class StepsShowTheProgressIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";
  private static final String PREVIOUS_STEPS_FIRST =
      "Please complete the previous checkout steps first.";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void forgetTheVisitor() {
    cookies.clear();
  }

  @Test
  @DisplayName("Progress bar on the buyer step")
  void theBuyerStepShowsFourNumberedStepsWithTheFirstAsCurrent() throws Exception {
    final Document buyer = buyerStepJustStarted();

    final Element progress = buyer.selectFirst("[data-test=checkout-progress]");
    assertThat(progress.select(".checkout__step .checkout__step-label").eachText())
        .containsExactly("Buyer Info", "Delivery", "Payment", "Review");
    assertThat(progress.select(".checkout__step .checkout__step-marker").eachText())
        .containsExactly("1", "2", "3", "4");
    assertThat(progress.select(".checkout__step--active .checkout__step-label").eachText())
        .containsExactly("Buyer Info");
    assertThat(progress.select(".checkout__step--completed")).isEmpty();
  }

  @Test
  @DisplayName("Progress bar on the confirmation")
  void theConfirmationMarksAllFourStepsAsDone() throws Exception {
    final Element confirmForm = reviewStep().selectFirst("[data-test=review-confirm-form]");

    final MvcResult placed = submit(confirmForm);

    assertThat(URI.create(placed.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(CONFIRMATION);
    final Element progress = page(CONFIRMATION).selectFirst("[data-test=checkout-progress]");
    assertThat(progress.select(".checkout__step .checkout__step-label").eachText())
        .containsExactly("Buyer Info", "Delivery", "Payment", "Confirmed");
    assertThat(progress.select(".checkout__step--completed .checkout__step-label").eachText())
        .containsExactly("Buyer Info", "Delivery", "Payment", "Confirmed");
  }

  @Test
  @DisplayName("Skipping ahead opens the current step")
  void openingTheReviewFromTheDeliveryStepOpensTheDeliveryStep() throws Exception {
    deliveryStepAfterTheBuyerStep();

    final MvcResult opened = perform(get(REVIEW_STEP));

    assertThat(URI.create(opened.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(DELIVERY_STEP);
    assertThat(page(DELIVERY_STEP).selectFirst("[data-test=delivery-form]")).isNotNull();
  }

  @Test
  @DisplayName("Payment from the buyer step opens the buyer step")
  void openingThePaymentStepFromTheBuyerStepOpensTheBuyerStep() throws Exception {
    buyerStepJustStarted();

    final MvcResult opened = perform(get(PAYMENT_STEP));

    assertThat(URI.create(opened.getResponse().getRedirectedUrl()).getPath()).isEqualTo(BUYER_STEP);
    assertThat(page(BUYER_STEP).selectFirst("[data-test=buyer-form]")).isNotNull();
  }

  @Test
  @DisplayName("Delivery sent before the buyer step")
  void aDeliveryFormSentBeforeTheBuyerStepEndsOnTheBuyerStep() throws Exception {
    final Element buyerForm = buyerStepJustStarted().selectFirst("[data-test=buyer-form]");
    final MockHttpServletRequestBuilder delivery = withHiddenFieldsOf(buyerForm, DELIVERY_STEP);
    delivery
        .param("street", "Hauptstrasse 1")
        .param("city", "Berlin")
        .param("postalCode", "10115")
        .param("country", "Germany")
        .param("shippingOptionId", "standard")
        .param("shippingOptionName", "Standard Shipping")
        .param("estimatedDelivery", "5-7 business days")
        .param("shippingCost", "4.99")
        .param("currencyCode", "EUR");

    final Landing landing = landingOf(perform(delivery));

    assertThat(landing.path()).isEqualTo(BUYER_STEP);
    assertThat(landing.page().select("[data-test=buyer-error-message]").text())
        .isEqualTo(PREVIOUS_STEPS_FIRST);
    assertThat(URI.create(perform(get(REVIEW_STEP)).getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(BUYER_STEP);
  }

  @Test
  @DisplayName("Place order before the review")
  void placeOrderSentFromThePaymentStepEndsOnThePaymentStep() throws Exception {
    final Element paymentForm = paymentStep().selectFirst("[data-test=payment-form]");

    final Landing landing =
        landingOf(perform(withHiddenFieldsOf(paymentForm, "/checkout/confirm")));

    assertThat(landing.path()).isEqualTo(PAYMENT_STEP);
    assertThat(landing.page().select("[data-test=payment-error-message]").text())
        .isEqualTo(PREVIOUS_STEPS_FIRST);
    final MvcResult confirmation = perform(get(CONFIRMATION));
    assertThat(confirmation.getResponse().getRedirectedUrl()).isNotEqualTo(CONFIRMATION);
    assertThat(confirmation.getResponse().getContentAsString())
        .doesNotContain("Your order has been placed successfully");
  }

  /** A POST to the path carrying only the form's hidden fields — its form token among them. */
  private static MockHttpServletRequestBuilder withHiddenFieldsOf(
      final Element form, final String path) {
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(path);
    for (final Element field : form.select("input[type=hidden][name]")) {
      request.param(field.attr("name"), field.val());
    }
    return request;
  }

  /** The page a chain of redirects finally answers with, and its path without the query. */
  private record Landing(String path, Document page) {}

  /**
   * Follows the redirects of a response to the page that finally answers — with nothing but the
   * cookies, as a second instance of the shop would see each request.
   */
  private Landing landingOf(final MvcResult response) throws Exception {
    MvcResult current = response;
    String url = null;
    for (int hops = 0; current.getResponse().getRedirectedUrl() != null; hops++) {
      assertThat(hops).as("redirects from " + url).isLessThan(5);
      url = current.getResponse().getRedirectedUrl();
      current = perform(get(url));
    }
    assertThat(current.getResponse().getStatus()).as("GET " + url).isEqualTo(200);
    return new Landing(
        URI.create(url).getPath(), Jsoup.parse(current.getResponse().getContentAsString()));
  }

  /**
   * A guest who started the checkout with "Domain-Driven Design" once, submitted the buyer, the
   * delivery step with "Standard Shipping", chose "Mock Payment (Test)" and opened the review.
   */
  private Document reviewStep() throws Exception {
    final Element paymentForm = paymentStep().selectFirst("[data-test=payment-form]");
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
   * A guest who started the checkout, submitted the buyer step and the delivery step with "Standard
   * Shipping", and is on the payment step.
   */
  private Document paymentStep() throws Exception {
    final Document delivery = deliveryStepAfterTheBuyerStep();
    final Element form = delivery.selectFirst("[data-test=delivery-form]");
    form.selectFirst("input[name=street]").val("Hauptstrasse 1");
    form.selectFirst("input[name=city]").val("Berlin");
    form.selectFirst("input[name=postalCode]").val("10115");
    form.selectFirst("input[name=country]").val("Germany");
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    for (final Element field : form.select("input[name]")) {
      if (field.closest("[data-test=delivery-shipping-option]") == null) {
        request.param(field.attr("name"), field.val());
      }
    }
    final Element method =
        form.select("[data-test=delivery-shipping-option]").stream()
            .filter(o -> "Standard Shipping".equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no shipping method named Standard Shipping"));
    for (final Element field : method.select("input[name]")) {
      request.param(field.attr("name"), field.val());
    }
    assertThat(URI.create(perform(request).getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(PAYMENT_STEP);

    return page(PAYMENT_STEP);
  }

  /** A guest who started the checkout, submitted the buyer step and is on the delivery step. */
  private Document deliveryStepAfterTheBuyerStep() throws Exception {
    final Element buyerForm = buyerStepJustStarted().selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val("jane@example.com");
    buyerForm.selectFirst("input[name=firstName]").val("Jane");
    buyerForm.selectFirst("input[name=lastName]").val("Doe");
    buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
    assertThat(submit(buyerForm).getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);

    return page(DELIVERY_STEP);
  }

  /** A guest who has just started the checkout with "Domain-Driven Design" once. */
  private Document buyerStepJustStarted() throws Exception {
    addToCart("Domain-Driven Design");
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(URI.create(started.getResponse().getRedirectedUrl()).getPath())
        .isEqualTo(BUYER_STEP);

    return page(BUYER_STEP);
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
