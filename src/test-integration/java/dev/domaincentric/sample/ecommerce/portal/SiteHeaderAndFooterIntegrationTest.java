package dev.domaincentric.sample.ecommerce.portal;

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
 * The header and footer every storefront page shares, rendered by the wired application: the header
 * navigation that reaches the home page and the catalogue, and the footer that names the shop.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@AutoConfigureMockMvc
@DisplayName("Site header and footer")
class SiteHeaderAndFooterIntegrationTest {

  private static final Pattern H1 = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.DOTALL);
  private static final Pattern NAV = Pattern.compile("<nav[^>]*>.*?</nav>", Pattern.DOTALL);
  private static final Pattern FOOTER_TEXT =
      Pattern.compile("class=\"site-footer__text\">([^<]*)<");

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("Nav home opens the home page")
  void homeInTheHeaderNavigationOpensTheHomePage() throws Exception {
    final String navigation = navigation(open("/products"));

    assertThat(linkLabel(navigation, "nav-home-link")).isEqualTo("Home");

    assertThat(heading(open(linkTarget(navigation, "nav-home-link"))))
        .isEqualTo("Welcome to domaincentric.commerce");
  }

  @Test
  @DisplayName("Nav products opens the catalogue")
  void productsInTheHeaderNavigationOpensTheCatalogue() throws Exception {
    final String navigation = navigation(open("/"));

    assertThat(linkLabel(navigation, "nav-products-link")).isEqualTo("Products");

    assertThat(heading(open(linkTarget(navigation, "nav-products-link"))))
        .isEqualTo("Our Products");
  }

  @Test
  @DisplayName("Footer names the shop")
  void footerOfTheCatalogueNamesTheShopAndLinksToTheEventLog() throws Exception {
    final String footer = element(open("/products"), "footer", "site-footer");

    assertThat(text(FOOTER_TEXT, footer))
        .isEqualTo("domaincentric.commerce — Built with Domain-Centric Architecture");
    assertThat(linkLabel(footer, "footer-event-log-link")).isEqualTo("Event Log");
    assertThat(linkTarget(footer, "footer-event-log-link")).isEqualTo("/backoffice/events");
  }

  @Test
  @DisplayName("Header and footer on the home page")
  void homePageShowsTheSameHeaderAndFooterAsTheCatalogue() throws Exception {
    final String home = open("/");
    final String catalogue = open("/products");
    final String homeHeader = element(home, "header", "site-header");
    final String catalogueHeader = element(catalogue, "header", "site-header");

    assertThat(linkLabel(homeHeader, "nav-home-link")).isEqualTo("Home");
    assertThat(linkLabel(homeHeader, "nav-products-link")).isEqualTo("Products");
    assertThat(logo(homeHeader)).isEqualTo(logo(catalogueHeader));
    assertThat(navigation(homeHeader)).isEqualTo(navigation(catalogueHeader));
    assertThat(footerParts(home)).isEqualTo(footerParts(catalogue));
  }

  private String open(final String path) throws Exception {
    return mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
  }

  /** The element with the given tag marked with the given {@code data-test} value. */
  private static String element(final String page, final String tag, final String dataTest) {
    final Matcher matcher =
        Pattern.compile(
                "<" + tag + "[^>]*data-test=\"" + dataTest + "\"[^>]*>(.*?)</" + tag + ">",
                Pattern.DOTALL)
            .matcher(page);
    assertThat(matcher.find()).as("%s %s in the rendered page", tag, dataTest).isTrue();
    return matcher.group(1);
  }

  private static String navigation(final String html) {
    final Matcher matcher = NAV.matcher(html);
    assertThat(matcher.find()).as("the header navigation").isTrue();
    return matcher.group();
  }

  /** The logo's target and the name it reads. */
  private static String logo(final String header) {
    final Matcher matcher =
        Pattern.compile(
                "<a[^>]*\\bhref=\"([^\"]*)\"[^>]*data-test=\"site-logo\"[^>]*>(.*?)</a>",
                Pattern.DOTALL)
            .matcher(header);
    assertThat(matcher.find()).as("the logo").isTrue();
    return matcher.group(1) + " " + visibleText(matcher.group(2));
  }

  /** The footer's text and its event log link, the parts this story names. */
  private static String footerParts(final String page) {
    final String footer = element(page, "footer", "site-footer");
    return text(FOOTER_TEXT, footer)
        + " | "
        + linkLabel(footer, "footer-event-log-link")
        + " -> "
        + linkTarget(footer, "footer-event-log-link");
  }

  private static String heading(final String page) {
    return visibleText(text(H1, page));
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

  private static String text(final Pattern pattern, final String html) {
    final Matcher matcher = pattern.matcher(html);
    assertThat(matcher.find()).as("%s in the rendered page", pattern).isTrue();
    return HtmlUtils.htmlUnescape(matcher.group(1)).strip();
  }

  /** The text of a fragment as a reader sees it: markup removed, whitespace collapsed. */
  private static String visibleText(final String html) {
    return HtmlUtils.htmlUnescape(html.replaceAll("<[^>]+>", "")).replaceAll("\\s+", " ").strip();
  }
}
