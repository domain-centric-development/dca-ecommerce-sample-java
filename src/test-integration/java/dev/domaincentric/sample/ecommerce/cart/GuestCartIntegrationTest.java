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
 * Whose cart the cart page shows, rendered by the wired application: each browser is its own cookie
 * jar, carried from request to request; the tabs of one browser share its jar.
 */
// MockMvc requests commit, and the shared H2 database (DB_CLOSE_DELAY=-1) outlives the Spring
// context — so this class gets a database of its own.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:guest_cart_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Guest cart")
class GuestCartIntegrationTest {

  private static final String MUG = "\"Ubiquitous Language\" Mug";
  private static final String TEAM_TOPOLOGIES = "Team Topologies";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("A second browser opens an empty cart beside a guest's filled one")
  void anotherBrowserOpensAnEmptyCart() throws Exception {
    final Browser guest = new Browser();
    guest.addToCart(MUG);

    final Browser secondVisitor = new Browser();

    assertThat(secondVisitor.cartLines()).isEmpty();
  }

  @Test
  @DisplayName("What one guest adds stays out of another guest's cart")
  void cartsOfTwoGuestsStayApart() throws Exception {
    final Browser firstGuest = new Browser();
    firstGuest.addToCart(MUG);
    final Browser secondGuest = new Browser();

    secondGuest.addToCart(TEAM_TOPOLOGIES);

    assertThat(secondGuest.cartLines()).containsExactly(TEAM_TOPOLOGIES);
    assertThat(firstGuest.cartLines()).containsExactly(MUG);
  }

  @Test
  @DisplayName("Two tabs of one browser fill and show the same cart")
  void tabsOfOneBrowserShareTheCart() throws Exception {
    final Browser browser = new Browser();
    final Browser.Tab firstTab = browser.tab();
    final Browser.Tab secondTab = browser.tab();
    firstTab.addToCart(TEAM_TOPOLOGIES);

    secondTab.addToCart(MUG);

    assertThat(firstTab.cartLines()).containsExactlyInAnyOrder(TEAM_TOPOLOGIES, MUG);
    assertThat(secondTab.cartLines()).containsExactlyInAnyOrder(TEAM_TOPOLOGIES, MUG);
  }

  /** One browser: its cookies, shared by every tab it opens. */
  private final class Browser {

    private final Map<String, Cookie> cookies = new LinkedHashMap<>();
    private final Tab tab = new Tab();

    Tab tab() {
      return new Tab();
    }

    void addToCart(final String productName) throws Exception {
      tab.addToCart(productName);
    }

    List<String> cartLines() throws Exception {
      return tab.cartLines();
    }

    /** A tab requests with its browser's cookies and keeps what the shop sets in them. */
    private final class Tab {

      /** Presses "Add to Cart" on the product page of the product with that name. */
      void addToCart(final String productName) throws Exception {
        final Element card =
            page("/products").select("[data-test=product-card]").stream()
                .filter(
                    c -> productName.equals(c.selectFirst("[data-test=product-card-title]").text()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no card titled " + productName));
        final Document productPage =
            page(card.selectFirst("a[data-test=view-product]").attr("href"));
        final Element form = productPage.selectFirst("[data-test=product-add-to-cart-form]");

        final MockHttpServletRequestBuilder request =
            MockMvcRequestBuilders.post(form.attr("action"));
        for (final Element field : form.select("input[name]")) {
          request.param(field.attr("name"), field.val());
        }
        final MvcResult result = perform(request);
        assertThat(result.getResponse().getStatus())
            .as("POST " + form.attr("action"))
            .isEqualTo(302);
      }

      /** The names of the lines the cart page shows, in page order. */
      List<String> cartLines() throws Exception {
        return page("/cart").select("[data-test=cart-item] [data-test=cart-item-name]").stream()
            .map(Element::text)
            .toList();
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
}
