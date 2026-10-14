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
import java.util.concurrent.atomic.AtomicInteger;
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
 * The buyer step as a guest and as a logged-in customer, and the order each places, rendered by the
 * wired application. Each browser keeps its own cookies from request to request, as a browser
 * would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:buyer_step_knows_who_is_buying_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("The buyer step knows who is buying")
class BuyerStepKnowsWhoIsBuyingIntegrationTest {

  private static final String PASSWORD = "Secret123";
  private static final String CART = "/cart";
  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String DELIVERY_STEP = "/checkout/delivery";
  private static final String PAYMENT_STEP = "/checkout/payment";
  private static final String REVIEW_STEP = "/checkout/review";
  private static final String CONFIRMATION = "/checkout/confirmation";

  /** The class shares one database, so every test registers an address of its own. */
  private static final AtomicInteger ACCOUNTS = new AtomicInteger();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Guest is offered login and registration")
  void aGuestOnTheBuyerStepIsOfferedToLogInOrRegister() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart("Domain-Driven Design");

    final Landing buyerStep = guest.followed(guest.submit(checkoutFormOf(guest)));

    assertThat(buyerStep.path()).isEqualTo(BUYER_STEP);
    final Document page = buyerStep.page();
    assertThat(page.selectFirst("[data-test=buyer-auth-options] > p").text())
        .isEqualTo("Already have an account? Log in to speed up checkout.");
    final Element login = page.selectFirst("[data-test=buyer-login-link]");
    assertThat(login.text()).isEqualTo("Login");
    assertThat(login.attr("href")).isEqualTo("/login?returnUrl=/checkout/buyer");
    final Element register = page.selectFirst("[data-test=buyer-register-link]");
    assertThat(register.text()).isEqualTo("Register");
    assertThat(register.attr("href")).isEqualTo("/register?returnUrl=/checkout/buyer");
    assertThat(page.selectFirst("[data-test=buyer-email-input]").val()).isEmpty();
  }

  @Test
  @DisplayName("Customer orders with the account email")
  void aLoggedInCustomerWhoKeepsTheFilledInEmailOrdersWithIt() throws Exception {
    final String email = anAddress();
    final Browser customer = new Browser();
    customer.register("/register", email);
    customer.addToCart("Domain-Driven Design");
    final Landing buyerStep = customer.followed(customer.submit(checkoutFormOf(customer)));
    assertThat(buyerStep.path()).isEqualTo(BUYER_STEP);

    final Document confirmation = customer.completeTheCheckoutFrom(buyerStep.page());

    assertThat(confirmation.selectFirst("[data-test=confirmation-title]").text())
        .isEqualTo("Your order has been placed successfully");
    assertThat(paragraphsUnder(confirmation, "Contact Information")).contains(email);
  }

  @Test
  @DisplayName("Guest registers mid checkout and orders")
  void aGuestWhoRegistersFromTheBuyerStepFinishesTheOrderLoggedIn() throws Exception {
    final String email = anAddress();
    final Browser guest = new Browser();
    guest.addToCart("Domain-Driven Design");
    final Document guestBuyerStep = guest.followed(guest.submit(checkoutFormOf(guest))).page();
    final String registerPage =
        guestBuyerStep.selectFirst("[data-test=buyer-register-link]").attr("href");
    final Landing backOnTheBuyerStep = guest.register(registerPage, email);
    assertThat(backOnTheBuyerStep.path()).isEqualTo(BUYER_STEP);

    final Document confirmation = guest.completeTheCheckoutFrom(backOnTheBuyerStep.page());

    assertThat(confirmation.selectFirst("[data-test=confirmation-title]").text())
        .isEqualTo("Your order has been placed successfully");
    assertThat(paragraphsUnder(confirmation, "Contact Information")).contains(email);
    assertThat(confirmation.selectFirst("[data-test=user-greeting]").text())
        .isEqualTo("Welcome, " + email);
  }

  /** An address no other test of this class registers. */
  private static String anAddress() {
    return "anna.schmidt." + ACCOUNTS.incrementAndGet() + "@example.com";
  }

  /** The "Proceed to Checkout" form of the browser's cart page. */
  private static Element checkoutFormOf(final Browser browser) throws Exception {
    return browser.page(CART).selectFirst("[data-test=cart-checkout-form]");
  }

  /** The paragraphs that follow a heading, up to the next heading. */
  private static List<String> paragraphsUnder(final Document page, final String heading) {
    final Element title =
        page.select("h4").stream()
            .filter(h -> heading.equals(h.text()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no heading " + heading));
    final List<String> paragraphs = new ArrayList<>();
    for (Element next = title.nextElementSibling();
        next != null && "p".equals(next.tagName());
        next = next.nextElementSibling()) {
      paragraphs.add(next.text());
    }
    return paragraphs;
  }

  /** Where a redirect chain ended, and the page shown there. */
  private record Landing(String path, Document page) {}

  /** One browser against the wired application, with the cookies it was given. */
  private final class Browser {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /** Registers Anna Schmidt on the registration page at {@code registerPage}; follows on. */
    Landing register(final String registerPage, final String email) throws Exception {
      final Element form = page(registerPage).selectFirst("[data-test=register-form]");
      form.selectFirst("input[name=firstName]").val("Anna");
      form.selectFirst("input[name=lastName]").val("Schmidt");
      form.selectFirst("input[name=dateOfBirth]").val("1990-05-17");
      form.selectFirst("input[name=email]").val(email);
      form.selectFirst("input[name=password]").val(PASSWORD);
      form.selectFirst("input[name=confirmPassword]").val(PASSWORD);
      return followed(submit(form));
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

    /**
     * Keeps the email the buyer step shows, enters "Anna", "Schmidt" and "+49 30 1234567",
     * completes the delivery and payment steps and presses "Place Order"; returns the confirmation.
     */
    Document completeTheCheckoutFrom(final Document buyerStep) throws Exception {
      final Element buyerForm = buyerStep.selectFirst("[data-test=buyer-form]");
      buyerForm.selectFirst("input[name=firstName]").val("Anna");
      buyerForm.selectFirst("input[name=lastName]").val("Schmidt");
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
              .filter(
                  o -> "Mock Payment (Test)".equals(o.selectFirst(".option-card__label").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no provider named Mock Payment (Test)"));
      payment.param("providerId", provider.selectFirst("input[name=providerId]").val());
      assertThat(perform(payment).getResponse().getRedirectedUrl()).isEqualTo(REVIEW_STEP);

      final MvcResult placed =
          submit(page(REVIEW_STEP).selectFirst("[data-test=review-confirm-form]"));
      assertThat(placed.getResponse().getRedirectedUrl()).isEqualTo(CONFIRMATION);
      return page(CONFIRMATION);
    }

    /** Submits a form with the values its fields carry, as the browser would. */
    MvcResult submit(final Element form) throws Exception {
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(URI.create(form.attr("action")));
      for (final Element field : form.select("input[name]")) {
        request.param(field.attr("name"), field.val());
      }
      final MvcResult result = perform(request);
      assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
      return result;
    }

    /** Follows redirects with the messages each carries, until a page is shown. */
    Landing followed(final MvcResult redirect) throws Exception {
      MvcResult current = redirect;
      String target = current.getResponse().getRedirectedUrl();
      while (target != null) {
        // The redirect is encoded already; a URI keeps it from being encoded twice.
        final MockHttpServletRequestBuilder hop = get(URI.create(target));
        current = perform(hop);
        final String next = current.getResponse().getRedirectedUrl();
        if (next == null) {
          assertThat(current.getResponse().getStatus()).as("GET " + target).isEqualTo(200);
          return new Landing(
              URI.create(target).getPath(),
              Jsoup.parse(current.getResponse().getContentAsString()));
        }
        target = next;
      }
      throw new AssertionError("the form answered without a redirect");
    }

    Document page(final String path) throws Exception {
      final MvcResult result = perform(get(URI.create(path)));
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
