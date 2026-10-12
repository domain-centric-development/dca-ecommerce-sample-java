package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelInputPort;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * A product with no stock left, as the catalogue card and its own page show it: rendered by the
 * wired application after Inventory's stock level for the seeded product was set to nothing.
 */
// A context of its own: the stock taken away here must not reach the classes asserting the seeded
// stock.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:out_of_stock_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Out of stock product")
class OutOfStockProductIntegrationTest {

  private static final String SOLD_OUT = "Wooden Hexagon Desk Model";

  private static final String CARD = "data-test=\"product-card\"";
  private static final Pattern CARD_TITLE =
      Pattern.compile("data-test=\"product-card-title\">([^<]*)<");
  private static final Pattern CARD_BADGE =
      Pattern.compile("class=\"product-card__badge[^\"]*\">([^<]*)<");
  private static final Pattern VIEW_LINK =
      Pattern.compile("<a[^>]*\\bhref=\"/products/([^\"]*)\"[^>]*data-test=\"view-product\"");
  private static final Pattern PAGE_BADGE =
      Pattern.compile(
          "class=\"product-detail__header\">.*?class=\"badge[^\"]*\">([^<]*)<", Pattern.DOTALL);
  private static final Pattern META_ITEM =
      Pattern.compile(
          "class=\"product-detail__meta-label\">([^<]*)</span>\\s*"
              + "<span class=\"product-detail__meta-value[^\"]*\">([^<]*)</span>",
          Pattern.DOTALL);
  private static final Pattern ACTIONS =
      Pattern.compile("class=\"product-detail__actions\">(.*?</a>)", Pattern.DOTALL);
  private static final Pattern BACK_LINK =
      Pattern.compile("<a[^>]*data-test=\"product-back-link\"[^>]*>([^<]*)</a>");
  private static final String ADD_TO_CART_BUTTON = "data-test=\"product-add-to-cart-button\"";

  @Autowired private MockMvc mockMvc;

  @Autowired private SetStockLevelInputPort setStockLevel;

  private String productId;

  @BeforeEach
  void noUnitIsLeft() throws Exception {
    productId = text(VIEW_LINK, cardOf(SOLD_OUT));
    setStockLevel.execute(new SetStockLevelCommand(productId, 0));
  }

  @Test
  @DisplayName("Card shows out of stock")
  void cardOfAProductWithNoStockLeftShowsOutOfStock() throws Exception {
    assertThat(text(CARD_BADGE, cardOf(SOLD_OUT))).isEqualTo("Out of Stock");
  }

  @Test
  @DisplayName("Out of stock product page")
  void productPageOfAProductWithNoStockLeftOffersOnlyTheWayBack() throws Exception {
    final String page = render("/products/" + productId);

    assertThat(text(PAGE_BADGE, page)).isEqualTo("Out of Stock");
    assertThat(metaValue(page, "Availability")).isEqualTo("Currently out of stock");
    final String actions = text(ACTIONS, page);
    assertThat(actions)
        .as("the actions offer no Add to Cart button")
        .doesNotContain(ADD_TO_CART_BUTTON);
    assertThat(text(BACK_LINK, actions)).isEqualTo("Back to Products");
  }

  private String cardOf(final String name) throws Exception {
    return Arrays.stream(render("/products").split(Pattern.quote(CARD)))
        .skip(1)
        .filter(c -> name.equals(text(CARD_TITLE, c)))
        .findFirst()
        .orElseThrow(() -> new AssertionError("no card titled " + name));
  }

  private String render(final String path) throws Exception {
    return mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  private static String metaValue(final String page, final String label) {
    final Matcher item = META_ITEM.matcher(page);
    while (item.find()) {
      if (label.equals(HtmlUtils.htmlUnescape(item.group(1)).strip())) {
        return HtmlUtils.htmlUnescape(item.group(2)).strip();
      }
    }
    throw new AssertionError("no meta item labelled " + label);
  }
}
