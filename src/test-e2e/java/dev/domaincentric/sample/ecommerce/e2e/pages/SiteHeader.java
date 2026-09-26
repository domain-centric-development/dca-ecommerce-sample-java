package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * The header every storefront page shares, whichever page is open.
 *
 * <p>Reads the logo and follows it home.
 */
public class SiteHeader extends BasePage {

  private static final String SITE_HEADER = "site-header";
  private static final String LOGO = "site-logo";

  /**
   * Creates the header of the page that is open and waits for it.
   *
   * @param page the Playwright page instance
   */
  public SiteHeader(Page page) {
    super(page);
    waitFor(SITE_HEADER);
  }

  /**
   * The logo's name as the visitor reads it.
   *
   * @return the logo text, without whitespace between its parts
   */
  public String logoName() {
    return page.locator("[data-test='" + LOGO + "']").textContent().replaceAll("\\s+", "");
  }

  /**
   * Follows the logo.
   *
   * @return the home page it opens
   */
  public HomePage followLogo() {
    click(LOGO);
    return new HomePage(page);
  }
}
