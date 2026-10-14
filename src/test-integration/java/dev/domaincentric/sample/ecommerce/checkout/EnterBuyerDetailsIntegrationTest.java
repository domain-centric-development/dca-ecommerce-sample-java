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
 * The buyer step of the checkout, rendered by the wired application — the visitor's cookies carried
 * from request to request, as a browser would, and the buyer form sent without the browser's own
 * checks.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:enter_buyer_details_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Enter the buyer details")
class EnterBuyerDetailsIntegrationTest {

  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String REFUSAL = "Please check the details you entered.";

  @Autowired private MockMvc mockMvc;

  private final Map<String, Cookie> cookies = new LinkedHashMap<>();

  @BeforeEach
  void startTheCheckoutWithDomainDrivenDesign() throws Exception {
    cookies.clear();
    addToCart("Domain-Driven Design");
    final MvcResult started = submit(page("/cart").selectFirst("[data-test=cart-checkout-form]"));
    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);
  }

  @Test
  @DisplayName("Buyer step shows the form")
  void theBuyerStepAsksForEmailNamesAndPhoneAllEmptyAndRequired() throws Exception {
    final Document buyer = page(BUYER_STEP);

    assertThat(buyer.selectFirst("h3.checkout__section-title").text())
        .isEqualTo("Buyer Information");
    final Element form = buyer.selectFirst("[data-test=buyer-form]");
    assertThat(
            form.select("label.form__label").stream()
                .map(
                    label -> {
                      final Element input = form.getElementById(label.attr("for"));
                      return label.text() + "|" + input.val() + "|" + input.hasAttr("required");
                    })
                .toList())
        .containsExactly(
            "Email Address||true", "First Name||true", "Last Name||true", "Phone Number||true");
    assertThat(buyer.selectFirst("[data-test=buyer-continue-button]").text())
        .isEqualTo("Continue to Delivery");
  }

  @Test
  @DisplayName("Email without at sign is refused")
  void anEmailWithoutAnAtSignOpensTheBuyerStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendBuyerForm("jane.example.com", "Jane", "Doe", "+49 30 1234567");

    assertThat(landed.selectFirst("[data-test=buyer-error-message]").text()).isEqualTo(REFUSAL);
  }

  @Test
  @DisplayName("Blank first name is refused")
  void aFirstNameOfSpacesOnlyOpensTheBuyerStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendBuyerForm("jane@example.com", "   ", "Doe", "+49 30 1234567");

    assertThat(landed.selectFirst("[data-test=buyer-error-message]").text()).isEqualTo(REFUSAL);
  }

  @Test
  @DisplayName("Blank phone is refused")
  void aPhoneNumberOfSpacesOnlyOpensTheBuyerStepAgainWithTheRefusal() throws Exception {
    final Document landed = sendBuyerForm("jane@example.com", "Jane", "Doe", "   ");

    assertThat(landed.selectFirst("[data-test=buyer-error-message]").text()).isEqualTo(REFUSAL);
  }

  /**
   * Sends the buyer form with the given values, past the browser's own checks, and follows the
   * redirect to the buyer step — the session along, which carries the page's message. Fails when
   * the shop sends the guest anywhere but back to the buyer step.
   */
  private Document sendBuyerForm(
      final String email, final String firstName, final String lastName, final String phone)
      throws Exception {
    final Element form = page(BUYER_STEP).selectFirst("[data-test=buyer-form]");
    final Map<String, String> values = new LinkedHashMap<>();
    for (final Element field : form.select("input[name]")) {
      values.put(field.attr("name"), field.val());
    }
    values.put("email", email);
    values.put("firstName", firstName);
    values.put("lastName", lastName);
    values.put("phone", phone);
    final MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(form.attr("action"));
    values.forEach(request::param);
    final MvcResult posted = perform(request);
    assertThat(posted.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);

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
