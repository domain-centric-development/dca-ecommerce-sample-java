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
  private static final String GREETING = "user-greeting";
  private static final String REGISTER_LINK = "nav-register-link";

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
   * The greeting a logged-in visitor reads.
   *
   * @return the greeting text
   */
  public String greeting() {
    waitFor(GREETING);
    return page.locator("[data-test='" + GREETING + "']").textContent().strip();
  }

  /**
   * Whether the header offers the "Register" link.
   *
   * @return true when the link is shown
   */
  public boolean offersRegister() {
    return exists(REGISTER_LINK);
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
