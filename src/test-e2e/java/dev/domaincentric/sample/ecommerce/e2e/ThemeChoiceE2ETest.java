package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.SelectOption;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The look a visitor chooses in the footer every page shares, and how the browser keeps it.
 *
 * <p>A page is in a theme when its root element names the theme — Blueprint, the default, names
 * none — and the page's background is that theme's.
 */
@DisplayName("Theme Choice E2E Tests")
class ThemeChoiceE2ETest extends BaseE2ETest {

  private enum Theme {
    BLUEPRINT("Blueprint", null, "rgb(247, 246, 243)"),
    ORGANIC("Organic", "organic", "rgb(250, 247, 242)"),
    SHARP_AND_MINIMAL("Sharp & Minimal", "sharp", "rgb(255, 255, 255)"),
    CLASSIC("Classic", "classic", "rgb(248, 250, 252)"),
    NEON_AND_DARK("Neon + Dark", "vibrant", "rgb(15, 15, 26)");

    private final String label;
    private final String attribute;
    private final String background;

    Theme(final String label, final String attribute, final String background) {
      this.label = label;
      this.attribute = attribute;
      this.background = background;
    }
  }

  @Test
  @DisplayName("The footer offers five themes")
  void theFooterOffersFiveThemesWithBlueprintSelected() {
    ProductCatalogPage.navigateTo(page);

    final Locator choice = themeChoice(page);
    assertThat(page.locator("[data-test='site-footer'] .theme-switcher__label")).hasText("Theme");
    assertThat(choice.locator("option"))
        .hasText(
            new String[] {"Blueprint", "Organic", "Sharp & Minimal", "Classic", "Neon + Dark"});
    assertSelected(page, Theme.BLUEPRINT);
    assertShownIn(page, Theme.BLUEPRINT);
  }

  @Test
  @DisplayName("Choosing a theme applies at once")
  void choosingAThemeAppliesItWithoutReloadingThePage() {
    ProductCatalogPage.navigateTo(page);
    assertShownIn(page, Theme.BLUEPRINT);
    page.evaluate("() => window.notReloaded = true");

    choose(page, Theme.ORGANIC);

    assertShownIn(page, Theme.ORGANIC);
    assertEquals(Boolean.TRUE, page.evaluate("() => window.notReloaded === true"), "not reloaded");
  }

  @Test
  @DisplayName("The choice follows to other pages")
  void aThemeChosenOnTheCatalogueFollowsToTheCartPage() {
    ProductCatalogPage.navigateTo(page);
    choose(page, Theme.NEON_AND_DARK);
    assertShownIn(page, Theme.NEON_AND_DARK);

    navigateTo("/cart");

    assertShownIn(page, Theme.NEON_AND_DARK);
    assertSelected(page, Theme.NEON_AND_DARK);
  }

  @Test
  @DisplayName("The choice survives a new visit")
  void aChosenThemeIsKeptWhenTheShopIsOpenedAgainInTheSameBrowser() {
    ProductCatalogPage.navigateTo(page);
    choose(page, Theme.CLASSIC);
    assertShownIn(page, Theme.CLASSIC);
    page.close();

    page = context.newPage();
    navigateTo("/");

    assertShownIn(page, Theme.CLASSIC);
    assertSelected(page, Theme.CLASSIC);
  }

  @Test
  @DisplayName("Choosing blueprint again returns to the default")
  void choosingBlueprintAgainShowsTheCatalogueInTheDefaultTheme() {
    ProductCatalogPage.navigateTo(page);
    choose(page, Theme.SHARP_AND_MINIMAL);
    assertShownIn(page, Theme.SHARP_AND_MINIMAL);

    choose(page, Theme.BLUEPRINT);
    ProductCatalogPage.navigateTo(page);

    assertShownIn(page, Theme.BLUEPRINT);
  }

  @Test
  @DisplayName("Another browser keeps the default")
  void aThemeChosenInOneBrowserLeavesAnotherOnTheDefault() {
    ProductCatalogPage.navigateTo(page);
    choose(page, Theme.ORGANIC);
    assertShownIn(page, Theme.ORGANIC);

    try (BrowserContext anotherBrowser = browser.newContext(contextOptions())) {
      final Page other = anotherBrowser.newPage();
      ProductCatalogPage.navigateTo(other);

      assertShownIn(other, Theme.BLUEPRINT);
    }
  }

  private static Locator themeChoice(final Page page) {
    final Locator choice = page.locator("[data-test='site-footer'] [data-test='theme-select']");
    assertThat(choice).isVisible();
    return choice;
  }

  private static void choose(final Page page, final Theme theme) {
    themeChoice(page).selectOption(new SelectOption().setLabel(theme.label));
  }

  private static void assertSelected(final Page page, final Theme theme) {
    assertThat(themeChoice(page).locator("option:checked")).hasText(theme.label);
  }

  private static void assertShownIn(final Page page, final Theme theme) {
    final Locator root = page.locator("html");
    if (theme.attribute == null) {
      assertThat(root).not().hasAttribute("data-theme", Pattern.compile(".*"));
    } else {
      assertThat(root).hasAttribute("data-theme", theme.attribute);
    }
    assertThat(page.locator("body")).hasCSS("background-color", theme.background);
  }
}
