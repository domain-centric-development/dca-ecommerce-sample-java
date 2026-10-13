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
 * Logging in while both the guest's cart and the account's cart hold items, rendered by the wired
 * application: the merge page that asks, and what each choice leaves in the cart. Each browser
 * keeps its own cookies from request to request, and a redirect's message is carried to the page it
 * leads to, as a browser would.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:cart_merge_choice_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Choose how two carts come together")
class CartMergeChoiceIntegrationTest {

  private static final String PASSWORD = "Secret123";
  private static final String MERGE_PAGE = "/cart/merge";
  private static final String CART = "/cart";
  private static final String CLEAN_ARCHITECTURE = "Clean Architecture";
  private static final String MUG = "\"Ubiquitous Language\" Mug";

  /** The class shares one database, so every test registers an address of its own. */
  private static final AtomicInteger ACCOUNTS = new AtomicInteger();

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Merge page when both carts hold items")
  void loggingInWithItemsInBothCartsAsksWhatToDoWithThem() throws Exception {
    final TwoCarts carts = theTwoCarts();

    final Landing landed = carts.guest().logIn(carts.email());

    assertThat(landed.path()).startsWith(MERGE_PAGE + "?");
    assertThat(textOf(landed.page(), ".merge-options__title")).isEqualTo("Your Cart Has Items");
    assertThat(textOf(landed.page(), ".merge-options__intro"))
        .isEqualTo(
            "You have items in both your current browsing session and your account. How would you"
                + " like to proceed?");
  }

  @Test
  @DisplayName("Merge page summarises both carts")
  void theMergePageSummarisesTheGuestsCartAndTheAccountsCart() throws Exception {
    final TwoCarts carts = theTwoCarts();

    final Document page = carts.guest().logIn(carts.email()).page();

    final Element session = page.selectFirst("[data-test=anonymous-cart-summary]");
    assertThat(textOf(session, ".merge-options__cart-title")).isEqualTo("Current Session Cart");
    assertThat(statsOf(session))
        .containsExactly("Items: 2", "Total Quantity: 3", "Total: 96.97 EUR");
    assertThat(rowsOf(session))
        .containsExactly(
            "Clean Architecture | Qty: 2 | 39.99 EUR | /images/products/clean-architecture-book.webp",
            MUG + " | Qty: 1 | 16.99 EUR | /images/products/ubiquitous-language-mug.webp");
    final Element account = page.selectFirst("[data-test=account-cart-summary]");
    assertThat(textOf(account, ".merge-options__cart-title")).isEqualTo("Account Cart");
    assertThat(statsOf(account))
        .containsExactly("Items: 1", "Total Quantity: 1", "Total: 39.99 EUR");
    assertThat(rowsOf(account))
        .containsExactly(
            "Clean Architecture | Qty: 1 | 39.99 EUR | /images/products/clean-architecture-book.webp");
  }

  @Test
  @DisplayName("Merge page offers three options")
  void theMergePageOffersThreeOptionsWithMergingBothChosen() throws Exception {
    final TwoCarts carts = theTwoCarts();

    final Document page = carts.guest().logIn(carts.email()).page();

    assertThat(textOf(page, ".merge-options__choose-title")).isEqualTo("Choose an Option");
    final Element mergeBoth = page.selectFirst("[data-test=merge-option-merge-both]");
    assertThat(optionOf(mergeBoth))
        .isEqualTo(
            "Merge Both Carts | Combine all items from both carts. If the same product exists in"
                + " both, quantities will be added together.");
    assertThat(mergeBoth.selectFirst("input[type=radio]").hasAttr("checked")).isTrue();
    assertThat(optionOf(page.selectFirst("[data-test=merge-option-use-account]")))
        .isEqualTo(
            "Use Account Cart Only | Keep your account cart and discard the items from your current"
                + " session.");
    assertThat(optionOf(page.selectFirst("[data-test=merge-option-use-anonymous]")))
        .isEqualTo(
            "Use Current Session Cart Only | Replace your account cart with the items from your"
                + " current session.");
    assertThat(textOf(page, "[data-test=merge-submit-button]")).isEqualTo("Continue");
  }

  @Test
  @DisplayName("Use account cart only")
  void usingTheAccountCartOnlyKeepsItAndDropsTheGuestLines() throws Exception {
    final TwoCarts carts = theTwoCarts();
    final Landing mergePage = carts.guest().logIn(carts.email());

    final Landing landed = carts.guest().choose(mergePage.page(), "USE_ACCOUNT_CART");

    assertThat(landed.path()).isEqualTo(CART);
    assertThat(textOf(landed.page(), "[data-test=cart-success-message]"))
        .isEqualTo("Using your account cart.");
    assertThat(linesOf(landed.page())).containsExactly("Clean Architecture x1");
  }

  @Test
  @DisplayName("Use current session cart only")
  void usingTheSessionCartOnlyReplacesTheAccountsCartWithTheGuestLines() throws Exception {
    final TwoCarts carts = theTwoCarts();
    final Landing mergePage = carts.guest().logIn(carts.email());

    final Landing landed = carts.guest().choose(mergePage.page(), "USE_ANONYMOUS_CART");

    assertThat(landed.path()).isEqualTo(CART);
    assertThat(textOf(landed.page(), "[data-test=cart-success-message]"))
        .isEqualTo("Using your recent cart.");
    assertThat(linesOf(landed.page())).containsExactly("Clean Architecture x2", MUG + " x1");
  }

  @Test
  @DisplayName("Unknown merge option is refused")
  void anOptionThePageDoesNotOfferIsRefused() throws Exception {
    final TwoCarts carts = theTwoCarts();
    final Landing mergePage = carts.guest().logIn(carts.email());

    final Landing landed = carts.guest().choose(mergePage.page(), "KEEP_BOTH_APART");

    assertThat(landed.path()).startsWith(MERGE_PAGE + "?");
    assertThat(textOf(landed.page(), ".merge-options__title")).isEqualTo("Your Cart Has Items");
    assertThat(textOf(landed.page(), "[data-test=merge-error-message]"))
        .isEqualTo("Invalid merge option selected");
  }

  /**
   * The two carts: an account registered earlier whose cart holds "Clean Architecture" once, added
   * while logged in in another browser; and a browser without cookies in which a guest added "Clean
   * Architecture" twice and the mug once.
   */
  private TwoCarts theTwoCarts() throws Exception {
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
    final Browser elsewhere = new Browser();
    elsewhere.logIn(email);
    elsewhere.addToCart(CLEAN_ARCHITECTURE);
    final Browser guest = new Browser();
    guest.addToCart(CLEAN_ARCHITECTURE);
    guest.addToCart(CLEAN_ARCHITECTURE);
    guest.addToCart(MUG);
    return new TwoCarts(email, guest);
  }

  private static String textOf(final Element scope, final String selector) {
    final Element element = scope.selectFirst(selector);
    assertThat(element).as(selector).isNotNull();
    return element.text();
  }

  /** A cart box's figures, each as the page words it. */
  private static List<String> statsOf(final Element box) {
    return box
        .select(
            ".merge-options__cart-stats p, .merge-options__cart-stats .merge-options__cart-total")
        .stream()
        .map(Element::text)
        .toList();
  }

  /** A cart box's lines, each as "name | Qty: n | price | the image's address". */
  private static List<String> rowsOf(final Element box) {
    return box.select(".merge-options__item-row").stream()
        .map(
            row -> {
              final Element image = row.selectFirst(".merge-options__item-image img");
              return row.selectFirst(".merge-options__item-name").text()
                  + " | "
                  + row.selectFirst(".merge-options__item-qty").text()
                  + " | "
                  + row.selectFirst(".merge-options__item-price").text()
                  + " | "
                  + (image == null ? "no image" : image.attr("src"));
            })
        .toList();
  }

  /** An option card as "title | description". */
  private static String optionOf(final Element card) {
    return textOf(card, ".merge-options__card-title")
        + " | "
        + textOf(card, ".merge-options__card-description");
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

  /** The account's address and the guest's browser. */
  private record TwoCarts(String email, Browser guest) {}

  /** Where a redirect chain ended, and the page shown there. */
  private record Landing(String path, Document page) {}

  /** One browser against the wired application, with the cookies it was given. */
  private final class Browser {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /** Logs in on the login page; follows the shop's redirects to the end. */
    Landing logIn(final String email) throws Exception {
      final Element form = page("/login").selectFirst("[data-test=login-form]");
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

    /**
     * Presses "Continue" on the merge page with {@code strategy} as the chosen option; follows the
     * shop's redirects to the end.
     */
    Landing choose(final Document mergePage, final String strategy) throws Exception {
      final Element form = mergePage.selectFirst("[data-test=merge-form]");
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(form.attr("action"));
      for (final Element field : form.select("input[name]:not([type=radio])")) {
        request.param(field.attr("name"), field.val());
      }
      request.param("strategy", strategy);
      final MvcResult result = perform(request);
      assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
      return followed(result);
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
        if (!current.getFlashMap().isEmpty()) {
          hop.flashAttrs(current.getFlashMap());
        }
        current = perform(hop);
        final String next = current.getResponse().getRedirectedUrl();
        if (next == null) {
          assertThat(current.getResponse().getStatus()).as("GET " + target).isEqualTo(200);
          return new Landing(target, Jsoup.parse(current.getResponse().getContentAsString()));
        }
        target = next;
      }
      throw new AssertionError("the form answered without a redirect");
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
