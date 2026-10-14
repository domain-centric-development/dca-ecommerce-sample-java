package dev.domaincentric.sample.ecommerce.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import jakarta.servlet.http.Cookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * Taking a line out of the cart with the "Remove" the cart page offers on it, through the wired
 * application and its store: each browser is its own cookie jar, carried from request to request.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:remove_cart_item_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Remove a cart line")
class RemoveCartItemIntegrationTest {

  private static final String DDD = "Domain-Driven Design";
  private static final String CLEAN_ARCHITECTURE = "Clean Architecture";
  private static final String TEAM_TOPOLOGIES = "Team Topologies";
  private static final String MUG = "\"Ubiquitous Language\" Mug";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Remove takes the whole quantity")
  void removingALineTakesItsWholeQuantityOut() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart(DDD);
    guest.addToCart(DDD);
    guest.addToCart(DDD);
    guest.addToCart(CLEAN_ARCHITECTURE);

    final Document cart = guest.followed(guest.remove(guest.cartPage(), DDD));

    assertThat(lines(cart)).containsExactly(CLEAN_ARCHITECTURE + " x1");
    assertThat(cart.select(".cart__summary-row span").eachText())
        .containsExactly("Total Items: 1", "Total Quantity: 1");
  }

  @Test
  @DisplayName("Removing the last line empties the cart")
  void removingTheLastLineLeavesAnEmptyCart() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart(DDD);

    final Document cart = guest.followed(guest.remove(guest.cartPage(), DDD));

    final Element emptyTitle = cart.selectFirst(".empty-state__title");
    assertThat(emptyTitle).as("the empty cart's title").isNotNull();
    assertThat(emptyTitle.text()).isEqualTo("Your cart is empty");
  }

  @Test
  @DisplayName("Removing a line already gone changes nothing")
  void removingALineAnotherTabRemovedAlreadyChangesNothing() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart(DDD);
    guest.addToCart(CLEAN_ARCHITECTURE);
    final Document firstTab = guest.cartPage();
    final Document secondTab = guest.cartPage();
    guest.followed(guest.remove(firstTab, CLEAN_ARCHITECTURE));

    final Document cart = guest.followed(guest.remove(secondTab, CLEAN_ARCHITECTURE));

    assertThat(lines(cart)).containsExactly(DDD + " x1");
  }

  @Test
  @DisplayName("A remove with a malformed line id changes nothing")
  void aRemoveNamingNoLineOfTheCartChangesNothing() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart(DDD);

    final Document cart = guest.followed(guest.removeNaming(guest.cartPage(), DDD, "no-such-line"));

    assertThat(lines(cart)).containsExactly(DDD + " x1");
  }

  @Test
  @DisplayName("A remove for another visitor's line changes nothing")
  void aRemoveForAnotherVisitorsLineChangesNothing() throws Exception {
    final Browser firstGuest = new Browser();
    firstGuest.addToCart(MUG);
    final Browser secondGuest = new Browser();
    secondGuest.addToCart(TEAM_TOPOLOGIES);
    final String mugLine = lineIdOf(firstGuest.cartPage(), MUG);

    secondGuest.followed(
        secondGuest.removeNaming(secondGuest.cartPage(), TEAM_TOPOLOGIES, mugLine));

    assertThat(lines(firstGuest.cartPage())).containsExactly(MUG + " x1");
    assertThat(lines(secondGuest.cartPage())).containsExactly(TEAM_TOPOLOGIES + " x1");
  }

  /** The lines the cart page shows, as {@code name xquantity}, in page order. */
  private static List<String> lines(final Document cart) {
    return cart.select("[data-test=cart-item]").stream()
        .map(
            item ->
                item.selectFirst("[data-test=cart-item-name]").text()
                    + " x"
                    + item.selectFirst("[data-test=cart-item-quantity]").text())
        .toList();
  }

  /** The "Remove" form on the line with that name. */
  private static Element removeFormOf(final Document cart, final String productName) {
    final Element form =
        cart.select("[data-test=cart-item]").stream()
            .filter(
                item -> productName.equals(item.selectFirst("[data-test=cart-item-name]").text()))
            .map(item -> item.selectFirst("form[data-test=cart-item-remove-form]"))
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElse(null);
    assertThat(form).as("the Remove form on the line " + productName).isNotNull();
    return form;
  }

  /** The line id the "Remove" form on the line with that name sends. */
  private static String lineIdOf(final Document cart, final String productName) {
    return removeFormOf(cart, productName).selectFirst("input[name=cartItemId]").val();
  }

  /** One browser: its cookies, carried from request to request. */
  private final class Browser {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();

    /** Presses "Add to Cart" on the product page of the product with that name. */
    void addToCart(final String productName) throws Exception {
      final Element card =
          page("/products").select("[data-test=product-card]").stream()
              .filter(
                  c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
              .findFirst()
              .orElseThrow(() -> new AssertionError("no card titled " + productName));
      final Document productPage = page(card.selectFirst("a[data-test=view-product]").attr("href"));
      submit(productPage.selectFirst("[data-test=product-add-to-cart-form]"), Map.of());
    }

    Document cartPage() throws Exception {
      return page("/cart");
    }

    /**
     * Presses "Remove" on the line with that name.
     *
     * @return where the shop redirects to
     */
    String remove(final Document cart, final String productName) throws Exception {
      return submit(removeFormOf(cart, productName), Map.of());
    }

    /**
     * Sends the "Remove" form on the line with that name, naming another line id instead.
     *
     * @return where the shop redirects to
     */
    String removeNaming(final Document cart, final String productName, final String lineId)
        throws Exception {
      return submit(removeFormOf(cart, productName), Map.of("cartItemId", lineId));
    }

    /** Opens the page a redirect led to, which must be the cart page. */
    Document followed(final String redirectedTo) throws Exception {
      assertThat(redirectedTo).as("where the shop leads after Remove").isEqualTo("/cart");
      return page(redirectedTo);
    }

    private String submit(final Element form, final Map<String, String> overrides)
        throws Exception {
      final MockHttpServletRequestBuilder request =
          MockMvcRequestBuilders.post(form.attr("action"));
      for (final Element field : form.select("input[name]")) {
        request.param(field.attr("name"), overrides.getOrDefault(field.attr("name"), field.val()));
      }
      final MvcResult result = perform(request);
      assertThat(result.getResponse().getStatus()).as("POST " + form.attr("action")).isEqualTo(302);
      return result.getResponse().getRedirectedUrl();
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
}
