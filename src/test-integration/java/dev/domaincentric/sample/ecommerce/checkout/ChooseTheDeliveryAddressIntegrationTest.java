package dev.domaincentric.sample.ecommerce.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * The delivery step of the checkout, rendered by the wired application — the visitor's cookies
 * carried from request to request, as a browser would, and the delivery form sent without the
 * browser's own checks.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:choose_the_delivery_address_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Choose the delivery address and the shipping method")
class ChooseTheDeliveryAddressIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String REFUSAL = "Please check the delivery address you entered.";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void submitTheBuyerStepWithDomainDrivenDesignInTheCart() throws Exception {
    cookies.clear();
    addToCart("Domain-Driven Design");
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);

    final Element buyerForm = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    buyerForm.selectFirst("input[name=email]").val("jane@example.com");
    buyerForm.selectFirst("input[name=firstName]").val("Jane");
    buyerForm.selectFirst("input[name=lastName]").val("Doe");
    buyerForm.selectFirst("input[name=phone]").val("+49 30 1234567");
    final MvcResult buyerSent = submit(buyerForm);
    assertThat(buyerSent.getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);
  }

  @Test
  @DisplayName("Delivery step shows the form")
  void theDeliveryStepAsksForTheAddressWithFourRequiredFieldsABackLinkAndAButton()
      throws Exception {
    final Document delivery = page(DELIVERY_STEP);

    final Element form = delivery.selectFirst("[data-test=delivery-form]");
    assertThat(form.selectFirst("h3.checkout__section-title").text()).isEqualTo("Shipping Address");
    assertThat(
            form.select("label.form__label").stream()
                .map(
                    label ->
                        label.text()
                            + "|"
                            + form.getElementById(label.attr("for")).hasAttr("required"))
                .toList())
        .containsExactly(
            "Street Address|true",
            "Address Line 2 (Optional)|false",
            "City|true",
            "State/Province (Optional)|false",
            "Postal Code|true",
            "Country|true");
    assertThat(delivery.selectFirst("[data-test=delivery-back-link]").text()).isEqualTo("Back");
    assertThat(delivery.selectFirst("[data-test=delivery-continue-button]").text())
        .isEqualTo("Continue to Payment");
  }

  @Test
  @DisplayName("Four shipping methods are offered")
  void theDeliveryStepOffersFourShippingMethodsOneOfWhichMustBeChosen() throws Exception {
    final Document delivery = page(DELIVERY_STEP);

    final Element form = delivery.selectFirst("[data-test=delivery-form]");
    final Element methods = form.selectFirst("[data-test=delivery-shipping-options]");
    assertThat(methods.previousElementSibling().text()).isEqualTo("Shipping Method");
    assertThat(
            methods.select("[data-test=delivery-shipping-option]").stream()
                .map(
                    option ->
                        option.selectFirst(".option-card__label").text()
                            + "|"
                            + option.selectFirst(".option-card__description").text()
                            + "|"
                            + option.selectFirst(".option-card__price").text())
                .toList())
        .containsExactly(
            "Standard Shipping|5-7 business days|4.99 EUR",
            "Express Shipping|2-3 business days|9.99 EUR",
            "Overnight Shipping|Next business day|19.99 EUR",
            "Free Shipping|7-10 business days|0.00 EUR");
    assertThat(methods.select("[data-test=delivery-shipping-radio]"))
        .hasSize(4)
        .allSatisfy(
            radio -> {
              assertThat(radio.attr("name")).isEqualTo("shippingOptionId");
              assertThat(radio.hasAttr("required")).isTrue();
            });
  }

  @Test
  @DisplayName("Delivery step summarises the order")
  void theDeliveryStepSummarisesTheOrderWithItsSubtotal() throws Exception {
    final Document delivery = page(DELIVERY_STEP);

    final Element summary = delivery.selectFirst(".order-summary");
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
    assertThat(summary.select(".order-summary__total > span").eachText())
        .containsExactly("Subtotal", "54.99 EUR");
  }

  @Test
  @DisplayName("Blank street is refused")
  void aStreetOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendDeliveryForm("   ", "Berlin", "10115", "Germany");

    assertThat(landed.selectFirst("[data-test=delivery-error-message]").text()).isEqualTo(REFUSAL);
  }

  @Test
  @DisplayName("Blank city is refused")
  void aCityOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendDeliveryForm("Hauptstrasse 1", "   ", "10115", "Germany");

    assertThat(landed.selectFirst("[data-test=delivery-error-message]").text()).isEqualTo(REFUSAL);
  }

  @Test
  @DisplayName("Blank postal code is refused")
  void aPostalCodeOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendDeliveryForm("Hauptstrasse 1", "Berlin", "   ", "Germany");

    assertThat(landed.selectFirst("[data-test=delivery-error-message]").text()).isEqualTo(REFUSAL);
  }

  @Test
  @DisplayName("Blank country is refused")
  void aCountryOfSpacesOnlyOpensTheDeliveryStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendDeliveryForm("Hauptstrasse 1", "Berlin", "10115", "   ");

    assertThat(landed.selectFirst("[data-test=delivery-error-message]").text()).isEqualTo(REFUSAL);
  }

  /**
   * Sends the delivery form with the given address and "Standard Shipping" chosen, past the
   * browser's own checks, and follows the redirect to the delivery step — the session along, which
   * carries the page's message. Fails when the shop sends the guest anywhere but back to the
   * delivery step.
   */
  private Document sendDeliveryForm(
      final String street, final String city, final String postalCode, final String country)
      throws Exception {
    final Element form = page(DELIVERY_STEP).selectFirst("[data-test=delivery-form]");
    final Map<String, String> values = new LinkedHashMap<>();
    for (final Element field : form.select("input[name]:not([disabled]):not([type=radio])")) {
      values.put(field.attr("name"), field.val());
    }
    values.put("street", street);
    values.put("city", city);
    values.put("postalCode", postalCode);
    values.put("country", country);
    final Element standard =
        form.select("[data-test=delivery-shipping-option]").stream()
            .filter(o -> "Standard Shipping".equals(o.selectFirst(".option-card__label").text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no shipping method named Standard Shipping"));
    for (final Element field : standard.select("input[name]")) {
      values.put(field.attr("name"), field.val());
    }
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    values.forEach(request::param);
    final MvcResult posted = perform(request);
    assertThat(posted.getResponse().getRedirectedUrl()).isEqualTo(DELIVERY_STEP);

    final MockHttpServletRequestBuilder follow = get(DELIVERY_STEP);
    final HttpSession session = posted.getRequest().getSession(false);
    if (session != null) {
      follow.session((MockHttpSession) session);
    }
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
