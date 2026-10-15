package dev.domaincentric.sample.ecommerce.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.net.URI;
import java.time.Duration;
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
 * Which cart a browser holds across registration, logout, a new login and a lapsed session,
 * rendered by the wired application. Each browser keeps its own cookies from request to request,
 * and a redirect's message is carried to the page it leads to, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_across_the_session_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("The cart stays with the person")
class CartAcrossTheSessionIntegrationTest {

  private static final String PASSWORD = "Secret123";
  private static final String CART = "/cart";
  private static final String BUYER_STEP = "/checkout/buyer";
  private static final String IDENTITY_COOKIE = "shop-identity";
  private static final String SESSION_COOKIE = "shop-session";

  /** The class shares one database, so every test registers an address of its own. */
  private static final AtomicInteger ACCOUNTS = new AtomicInteger();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Registering from the checkout keeps the checkout")
  void registeringFromTheBuyerStepReturnsToTheSameCheckout() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart("Domain-Driven Design");
    guest.submit(guest.page(CART).selectFirst("[data-test=cart-checkout-form]"));

    final Landing landed = guest.register("/register?returnUrl=" + BUYER_STEP, anAddress());

    assertThat(landed.path()).isEqualTo(BUYER_STEP);
    assertThat(summaryOf(landed.page())).containsExactly("Domain-Driven Design x1");
  }

  @Test
  @DisplayName("Registered customer adds to the cart")
  void aVisitorWhoJustRegisteredAddsToTheirCart() throws Exception {
    final Browser visitor = new Browser();
    visitor.register("/register", anAddress());

    visitor.addToCart("Clean Architecture");

    assertThat(linesOf(visitor.page(CART))).contains("Clean Architecture x1");
  }

  @Test
  @DisplayName("Taken-over cart comes back at the next login")
  void theCartTheAccountTookOverComesBackAfterLoggingOutAndIn() throws Exception {
    final String email = anAddress();
    final Browser guest = new Browser();
    guest.addToCart("Domain-Driven Design");
    guest.register("/register", email);
    guest.logOut();

    guest.logIn(email);

    assertThat(linesOf(guest.page(CART))).containsExactly("Domain-Driven Design x1");
  }

  @Test
  @DisplayName("Logout leaves an empty cart")
  void afterLogoutTheBrowserStartsWithAnEmptyCart() throws Exception {
    final Browser customer = new Browser();
    customer.register("/register", anAddress());
    customer.addToCart("Domain-Driven Design");
    customer.logOut();

    final Document cart = customer.page(CART);

    assertThat(cart.select(".empty-state__title").eachText()).containsExactly("Your cart is empty");
  }

  @Test
  @DisplayName("Lapsed login keeps the cart")
  void whenTheLoginLapsesTheVisitorKeepsTheCartTheyHad() throws Exception {
    final String email = anAddress();
    new Browser().register("/register", email);
    final Browser browser = new Browser();
    browser.addToCart("\"Ubiquitous Language\" Mug");
    browser.logIn(email);

    // The session cookie lives 7 days, the visitor identity 30: the browser drops the session
    // first.
    assertThat(browser.cookie(SESSION_COOKIE).getMaxAge())
        .isEqualTo((int) Duration.ofDays(7).toSeconds())
        .isLessThan(browser.cookie(IDENTITY_COOKIE).getMaxAge());
    browser.forget(SESSION_COOKIE);

    final Document cart = browser.page(CART);
    assertThat(cart.selectFirst("[data-test=nav-login-link]").text()).isEqualTo("Login");
    assertThat(cart.selectFirst("[data-test=nav-register-link]").text()).isEqualTo("Register");
    assertThat(linesOf(cart)).containsExactly("\"Ubiquitous Language\" Mug x1");
  }

  /** An address no other test of this class registers. */
  private static String anAddress() {
    return "anna.schmidt." + ACCOUNTS.incrementAndGet() + "@example.com";
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

  /** The buyer step's order summary, each line as "name xQuantity". */
  private static List<String> summaryOf(final Document buyerStep) {
    return buyerStep.select("[data-test=order-summary-item]").stream()
        .map(
            item ->
                item.selectFirst("[data-test=order-summary-item-name]").text()
                    + " "
                    + item.selectFirst("[data-test=order-summary-item-qty]").text())
        .toList();
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

    /**
     * Logs in on the login page; follows the shop's redirects to the end and sends the form of the
     * page after the login, as the script of a browser does.
     */
    Landing logIn(final String email) throws Exception {
      final Element form = page("/login").selectFirst("[data-test=login-form]");
      form.selectFirst("input[name=email]").val(email);
      form.selectFirst("input[name=password]").val(PASSWORD);
      final Landing landed = followed(submit(form));
      final Element takeOver = landed.page().selectFirst("[data-test=take-over-form]");
      return takeOver == null ? landed : followed(submit(takeOver));
    }

    /** Presses "Logout" in the header of the product list. */
    void logOut() throws Exception {
      submit(page("/products").selectFirst("[data-test=logout-form]"));
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

    /** The cookie this browser holds under that name. */
    Cookie cookie(final String name) {
      assertThat(cookies).as("cookies the browser holds").containsKey(name);
      return cookies.get(name);
    }

    /** Drops a cookie, as the browser does once its max-age has run out. */
    void forget(final String name) {
      cookies.remove(name);
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
