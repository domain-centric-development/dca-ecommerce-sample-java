package dev.domaincentric.sample.ecommerce.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
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
 * The not-found page a visitor gets for a product address no product of the seeded catalogue has,
 * rendered by the wired application: its tab title and the two ways on it offers.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Product not found page")
class ProductNotFoundPageIntegrationTest {

  private static final String UNKNOWN_PRODUCT = "/products/no-such-product";

  private static final Pattern PAGE_TITLE = Pattern.compile("<title>([^<]*)</title>");
  private static final Pattern H1 = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.DOTALL);

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Not-found page has the shop title")
  void browserTabOfTheNotFoundPageIsTitledWithTheShopsName() throws Exception {
    assertThat(text(PAGE_TITLE, notFoundPage())).isEqualTo("domaincentric.commerce");
  }

  @Test
  @DisplayName("Browse all products leads to the catalogue")
  void browseAllProductsOpensTheCatalogue() throws Exception {
    final String notFound = notFoundPage();

    assertThat(linkLabel(notFound, "error-browse-link")).isEqualTo("Browse All Products");

    assertThat(text(H1, open(linkTarget(notFound, "error-browse-link")))).isEqualTo("Our Products");
  }

  @Test
  @DisplayName("Go to homepage leads to the home page")
  void goToHomepageOpensTheHomePage() throws Exception {
    final String notFound = notFoundPage();

    assertThat(linkLabel(notFound, "error-home-link")).isEqualTo("Go to Homepage");

    assertThat(text(H1, open(linkTarget(notFound, "error-home-link"))))
        .isEqualTo("Welcome to domaincentric.commerce");
  }

  // No status is asserted: the scenarios name none, only what the page shows.
  private String notFoundPage() throws Exception {
    return mockMvc.perform(get(UNKNOWN_PRODUCT)).andReturn().getResponse().getContentAsString();
  }

  private String open(final String path) throws Exception {
    return mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  private static String linkTarget(final String html, final String dataTest) {
    return link(html, dataTest).group(1);
  }

  private static String linkLabel(final String html, final String dataTest) {
    return HtmlUtils.htmlUnescape(link(html, dataTest).group(2)).strip();
  }

  private static Matcher link(final String html, final String dataTest) {
    final Matcher matcher =
        Pattern.compile(
                "<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"" + dataTest + "\"[^>]*>([^<]*)</a>")
            .matcher(html);
    assertThat(matcher.find()).as("link %s", dataTest).isTrue();
    return matcher;
  }

  /** The visible text of the first match, markup inside it dropped and whitespace collapsed. */
  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1).replaceAll("<[^>]+>", ""))
        .replaceAll("\\s+", " ")
        .strip();
  }
}
