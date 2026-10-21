package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
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
 * Logging in, rendered by the wired application: which cart the visitor finds afterwards, and where
 * the login leads from the checkout. Each browser keeps its own cookies from request to request,
 * and a redirect's message is carried to the page it leads to, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_at_login_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Logging in brings the guest cart along")
class CartAtLoginIntegrationTest {

  private static final String PASSWORD = "Secret123";
  private static final String CART = "/cart";
  private static final String BUYER_STEP = "/checkout/buyer";

  /** The class shares one database, so every test registers an address of its own. */
  private static final AtomicInteger ACCOUNTS = new AtomicInteger();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Account cart shown after login")
  void anEmptyGuestCartShowsTheAccountsCartAsItIs() throws Exception {
    final String email = anAccount();
    final Browser elsewhere = new Browser();
    elsewhere.logIn("/login", email);
    elsewhere.addToCart("Clean Architecture");
    final Browser here = new Browser();
    here.page(CART);

    final Landing landed = here.logIn("/login", email);

    assertThat(landed.path()).isEqualTo(CART);
    assertThat(linesOf(landed.page())).containsExactly("Clean Architecture x1");
  }

  @Test
  @DisplayName("The page after the login moves no cart")
  void openingThePageAfterTheLoginLeavesTheAccountsCartAsItWas() throws Exception {
    final String email = anAccount();
    final Browser guest = new Browser();
    guest.addToCart("\"Ubiquitous Language\" Mug");

    final Landing landed = guest.logInWithoutScript("/login", email);

    assertThat(textOf(landed.page(), "take-over-message")).isEqualTo("Bringing your cart along.");
    assertThat(textOf(landed.page(), "take-over-continue-button")).isEqualTo("Continue");
    final Browser account = new Browser();
    account.logIn("/login", email);
    assertThat(linesOf(account.page(CART))).isEmpty();
  }

  @Test
  @DisplayName("Login from the checkout returns there over an account cart that was emptied")
  void loggingInFromTheBuyerStepReturnsThereWhenTheAccountsCartWasEmptied() throws Exception {
    final String email = anAccount();
    final Browser elsewhere = new Browser();
    elsewhere.logIn("/login", email);
    elsewhere.addToCart("Clean Architecture");
    elsewhere.submit(elsewhere.page(CART).selectFirst("[data-test=cart-item-remove-form]"));
    assertThat(linesOf(elsewhere.page(CART))).isEmpty();
    final Browser guest = new Browser();
    guest.addToCart("Domain-Driven Design");
    guest.submit(guest.page(CART).selectFirst("[data-test=cart-checkout-form]"));

    final Landing landed = guest.logIn("/login?returnUrl=" + BUYER_STEP, email);

    assertThat(landed.path()).isEqualTo(BUYER_STEP);
    assertThat(summaryOf(landed.page())).containsExactly("Domain-Driven Design x1");
  }

  @Test
  @DisplayName("Logged-in customer starts the checkout")
  void aLoggedInCustomerStartsTheCheckoutLikeAGuest() throws Exception {
    final String email = anAccount();
    final Browser visitor = new Browser();
    visitor.logIn("/login", email);
    visitor.addToCart("Domain-Driven Design");

    final MvcResult started =
        visitor.submit(visitor.page(CART).selectFirst("[data-test=cart-checkout-form]"));

    assertThat(started.getResponse().getRedirectedUrl()).isEqualTo(BUYER_STEP);
    assertThat(visitor.page(BUYER_STEP).selectFirst("[data-test=buyer-form]")).isNotNull();
  }

  /**
   * An account registered earlier from a browser of its own, whose cart holds nothing; returns its
   * address.
   */
  private String anAccount() throws Exception {
    final String email = "anna.schmidt." + ACCOUNTS.incrementAndGet() + "@example.com";
    final Browser registration = new Browser();
    final Element form = registration.page("/register").selectFirst("[data-test=register-form]");
    form.selectFirst("input[name=firstName]").val("Anna");
    form.selectFirst("input[name=lastName]").val("Schmidt");
    form.selectFirst("input[name=dateOfBirth]").val("1990-05-17");
    form.selectFirst("input[name=email]").val(email);
    form.selectFirst("input[name=password]").val(PASSWORD);
    form.selectFirst("input[name=confirmPassword]").val(PASSWORD);
    registration.submit(form);
    return email;
  }

  /** The cart page's lines, each as "name xQuantity". */
  private static List<String> linesOf(final Document cart) {
    return cart.select("[data-test=cart-item]").stream()
        .map(
            item ->
                item.selectFirst("[data-test=cart-item-name]").text()
                    + " x"
                    + item.selectFirst("[data-test=cart-item-quantity]").text())
        .toList();
  }

  /** A checkout step's order summary, each line as "name xQuantity". */
  private static List<String> summaryOf(final Document step) {
    return step.select("[data-test=order-summary-item]").stream()
        .map(
            item ->
                item.selectFirst("[data-test=order-summary-item-name]").text()
                    + " "
                    + item.selectFirst("[data-test=order-summary-item-qty]").text())
        .toList();
  }

  /** The text of the element with that {@code data-test} name, or null when the page has none. */
  private static String textOf(final Document page, final String dataTest) {
    final Element element = page.selectFirst("[data-test=" + dataTest + "]");
    return element == null ? null : element.text();
  }

  /** Where a redirect chain ended, and the page shown there. */
  private record Landing(String path, Document page) {}

  /** One browser against the wired application, with the cookies it was given. */
  private final class Browser {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /**
     * Logs in on the login page at {@code loginPage} as a browser that runs scripts: follows the
     * shop's redirects to the end and sends the form of the page after the login, as its script
     * does.
     */
    Landing logIn(final String loginPage, final String email) throws Exception {
      final Landing landed = logInWithoutScript(loginPage, email);
      final Element takeOver = landed.page().selectFirst("[data-test=take-over-form]");
      return takeOver == null ? landed : followed(submit(takeOver));
    }

    /**
     * Logs in on the login page at {@code loginPage} as a browser that runs no script; follows the
     * shop's redirects to the page that is shown.
     */
    Landing logInWithoutScript(final String loginPage, final String email) throws Exception {
      final Element form = page(loginPage).selectFirst("[data-test=login-form]");
      form.selectFirst("input[name=email]").val(email);
      form.selectFirst("input[name=password]").val(PASSWORD);
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

    /** Submits a form with the values its fields carry, as the browser would. */
    MvcResult submit(final Element form) throws Exception {
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(form.attr("action"));
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
      throw new AssertionError("the login answered without a redirect");
    }

    Document page(final String path) throws Exception {
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
}
