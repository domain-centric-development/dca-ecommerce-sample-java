package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.Arrays;
import java.util.List;
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
 * The catalogue page a visitor opens, rendered by the wired application from the sample catalogue
 * the shop seeds at start-up: what the page and each of its cards show.
 */
// A context of its own: another test class that adds a product to a shared context would change
// the seeded range this class asserts.
@SpringBootTest(
    classes = EcommerceSampleApplication.class,
    properties =
        "spring.datasource.url=jdbc:h2:mem:catalogue_page_test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
@AutoConfigureMockMvc
@DisplayName("Product catalogue page")
class ProductCatalogPageIntegrationTest {

  private static final String CARD = "data-test=\"product-card\"";
  private static final Pattern TITLE = Pattern.compile("data-test=\"product-card-title\">([^<]*)<");
  private static final Pattern DESCRIPTION =
      Pattern.compile("class=\"product-card__description\">([^<]*)<");
  private static final Pattern IMAGE = Pattern.compile("<img[^>]*\\bsrc=\"([^\"]*)\"");
  private static final Pattern VIEW_LINK =
      Pattern.compile(
          "<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"view-product\"[^>]*>([^<]*)</a>");
  private static final Pattern PAGE_TITLE = Pattern.compile("<title>([^<]*)</title>");
  private static final Pattern HEADING = Pattern.compile("<h1[^>]*>([^<]*)</h1>");
  private static final Pattern BREADCRUMB =
      Pattern.compile("data-test=\"breadcrumb\">(.*?)</div>", Pattern.DOTALL);
  private static final Pattern PRODUCT_PAGE = Pattern.compile("/products/[0-9a-f-]{36}");

  private static final String DDD_DESCRIPTION =
      "The seminal work by Eric Evans that introduced the software industry to Domain-Driven"
          + " Design. This essential guide teaches you how to tackle complexity in the heart of"
          + " software by connecting implementation to an evolving model of the business domain.";

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("The card titles are in ordinal order of the product name")
  void listsTheCardTitlesInOrdinalNameOrder() throws Exception {
    final List<String> titles =
        cards(catalogPage()).stream().map(card -> text(TITLE, card)).toList();

    assertThat(titles).isEqualTo(titles.stream().sorted().toList());
    assertThat(titles.getFirst()).isEqualTo("\"Bounded Context\" Enamel Pin");
  }

  @Test
  @DisplayName("A card shows the product's name, description and image")
  void cardShowsTheProductsNameDescriptionAndImage() throws Exception {
    final String card =
        cards(catalogPage()).stream()
            .filter(c -> "Domain-Driven Design".equals(text(TITLE, c)))
            .findFirst()
            .orElseThrow(() -> new AssertionError("no card titled Domain-Driven Design"));

    assertThat(text(TITLE, card)).isEqualTo("Domain-Driven Design");
    assertThat(text(DESCRIPTION, card)).isEqualTo(DDD_DESCRIPTION);
    assertThat(text(IMAGE, card)).isEqualTo("/images/products/ddd-book.webp");
  }

  @Test
  @DisplayName("Every card offers a View Details link to that product's page")
  void everyCardOffersAViewDetailsLinkToThatProductsPage() throws Exception {
    final List<String> cards = cards(catalogPage());
    assertThat(cards).isNotEmpty();

    for (final String card : cards) {
      final String title = text(TITLE, card);
      final Matcher link = VIEW_LINK.matcher(card);
      assertThat(link.find()).as("card %s has a view link", title).isTrue();
      assertThat(link.group(2).strip()).as("label on %s", title).isEqualTo("View Details");
      assertThat(link.group(1)).as("target of %s", title).matches(PRODUCT_PAGE);

      final String productPage =
          mockMvc
              .perform(get(link.group(1)))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertThat(HtmlUtils.htmlUnescape(productPage)).as("page behind %s", title).contains(title);
    }
  }

  @Test
  @DisplayName("The page is titled Product Catalog, headed Our Products, under Home / Products")
  void pageIsTitledHeadedAndPlacedAsTheCatalogue() throws Exception {
    final String page = catalogPage();

    assertThat(text(PAGE_TITLE, page)).isEqualTo("Product Catalog");
    assertThat(text(HEADING, page)).isEqualTo("Our Products");
    assertThat(breadcrumb(page)).isEqualTo("Home / Products");
  }

  private String catalogPage() throws Exception {
    return mockMvc
        .perform(get("/products"))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private static List<String> cards(final String page) {
    final List<String> parts = Arrays.asList(page.split(Pattern.quote(CARD)));
    return parts.subList(1, parts.size());
  }

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  /** The breadcrumb's visible parts, one space between each. */
  private static String breadcrumb(final String page) {
    final String inner = text(BREADCRUMB, page);
    return String.join(
        " ",
        Arrays.stream(inner.split("<[^>]+>"))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .toList());
  }
}
