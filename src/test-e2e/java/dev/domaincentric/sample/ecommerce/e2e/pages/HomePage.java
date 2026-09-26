package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the home page at {@code /}.
 *
 * <p>Reads the welcome the hero section shows.
 */
public class HomePage extends BasePage {

  private static final String URL_PATTERN = "/";
  private static final String HERO = "hero";

  /**
   * Creates a new HomePage and waits for its hero section.
   *
   * @param page the Playwright page instance
   */
  public HomePage(Page page) {
    super(page, URL_PATTERN);
    waitFor(HERO);
  }

  /**
   * Navigates to the home page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new HomePage
   */
  public static HomePage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new HomePage(page);
  }

  /**
   * The hero's heading as the visitor reads it.
   *
   * @return the heading text
   */
  public String heading() {
    return heroText("h1");
  }

  /**
   * The line under the heading.
   *
   * @return the subtitle text
   */
  public String subtitle() {
    return heroText(".hero__subtitle");
  }

  /**
   * The text below the subtitle.
   *
   * @return the description text
   */
  public String description() {
    return heroText(".hero__description");
  }

  // The text as written, not as CSS transforms it: the subtitle is styled in capitals.
  private String heroText(String selector) {
    return page.locator("[data-test='" + HERO + "'] " + selector).textContent().strip();
  }
}
