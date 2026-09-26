package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.HtmlUtils;

/**
 * The product page a visitor opens from a catalogue card, rendered by the wired application from
 * the sample catalogue the shop seeds at start-up: what the page shows and where it leads.
 */
// A context of its own, like the catalogue page's test: the seeded products stay as seeded.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:product_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Product page")
class ProductDetailPageIntegrationTest {

  private static final String CARD = "data-test=\"product-card\"";
  private static final Pattern CARD_TITLE =
      Pattern.compile("data-test=\"product-card-title\">([^<]*)<");
  private static final Pattern VIEW_LINK =
      Pattern.compile("<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"view-product\"");
  private static final Pattern PAGE_TITLE = Pattern.compile("<title>([^<]*)</title>");
  private static final Pattern HEADING = Pattern.compile("<h1[^>]*>([^<]*)</h1>");
  private static final Pattern IMAGE =
      Pattern.compile(
          "class=\"product-detail__image\"[^>]*>\\s*<img[^>]*\\bsrc=\"([^\"]*)\"", Pattern.DOTALL);
  private static final Pattern DESCRIPTION =
      Pattern.compile("class=\"product-detail__description\">\\s*<p>([^<]*)</p>", Pattern.DOTALL);
  private static final Pattern META_ITEM =
      Pattern.compile(
          "class=\"product-detail__meta-label\">([^<]*)</span>\\s*"
              + "<span class=\"product-detail__meta-value[^\"]*\">([^<]*)</span>",
          Pattern.DOTALL);
  private static final Pattern BREADCRUMB =
      Pattern.compile("data-test=\"breadcrumb\">(.*?)</div>", Pattern.DOTALL);
  private static final Pattern LINK =
      Pattern.compile("<a[^>]*\\bhref=\"([^\"]*)\"[^>]*>([^<]*)</a>");
  private static final Pattern BACK_LINK =
      Pattern.compile(
          "<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"product-back-link\"[^>]*>([^<]*)</a>");

  private static final String DDD_DESCRIPTION =
      "The seminal work by Eric Evans that introduced the software industry to Domain-Driven"
          + " Design. This essential guide teaches you how to tackle complexity in the heart of"
          + " software by connecting implementation to an evolving model of the business domain.";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The page shows the product's image, description and category")
  void showsTheProductsImageDescriptionAndCategory() throws Exception {
    final String page = productPageOf("Domain-Driven Design");

    assertThat(text(IMAGE, page)).isEqualTo("/images/products/ddd-book.webp");
    assertThat(text(DESCRIPTION, page)).isEqualTo(DDD_DESCRIPTION);
    assertThat(metaValue(page, "Category")).isEqualTo("Books");
  }

  @Test
  @DisplayName("The browser tab is titled with the product's name")
  void tabIsTitledWithTheProductsName() throws Exception {
    final String page = productPageOf("Clean Architecture");

    assertThat(text(PAGE_TITLE, page)).isEqualTo("Clean Architecture");
  }

  @Test
  @DisplayName("The breadcrumb reads Home / Products / the product, linking home and catalogue")
  void breadcrumbLeadsFromHomeThroughTheCatalogueToTheProduct() throws Exception {
    final String breadcrumb = text(BREADCRUMB, productPageOf("Clean Architecture"));

    assertThat(visibleParts(breadcrumb)).isEqualTo("Home / Products / Clean Architecture");
    assertThat(linkTarget(breadcrumb, "Home")).isEqualTo("/");
    assertThat(linkTarget(breadcrumb, "Products")).isEqualTo("/products");
  }

  @Test
  @DisplayName("Back to Products opens the catalogue page Our Products")
  void backToProductsOpensTheCatalogue() throws Exception {
    final Matcher back = BACK_LINK.matcher(productPageOf("Team Topologies"));
    assertThat(back.find()).as("the product page has a back link").isTrue();
    assertThat(HtmlUtils.htmlUnescape(back.group(2)).strip()).isEqualTo("Back to Products");

    final String catalogue = render(back.group(1));

    assertThat(text(HEADING, catalogue)).isEqualTo("Our Products");
  }

  /** Follows the "View Details" link on the catalogue card of the named product. */
  private String productPageOf(final String name) throws Exception {
    final String card =
        Arrays.stream(render("/products").split(Pattern.quote(CARD)))
            .skip(1)
            .filter(c -> name.equals(text(CARD_TITLE, c)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled " + name));
    return render(text(VIEW_LINK, card));
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

  private static String linkTarget(final String html, final String label) {
    final Matcher link = LINK.matcher(html);
    while (link.find()) {
      if (label.equals(HtmlUtils.htmlUnescape(link.group(2)).strip())) {
        return link.group(1);
      }
    }
    throw new AssertionError("no link labelled " + label);
  }

  /** The visible parts of a fragment, one space between each. */
  private static String visibleParts(final String html) {
    return String.join(
        " ",
        Arrays.stream(html.split("<[^>]+>"))
            .map(s -> HtmlUtils.htmlUnescape(s).strip())
            .filter(s -> !s.isEmpty())
            .toList());
  }
}
