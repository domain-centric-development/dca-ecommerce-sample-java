package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * The header of a back-office page, whichever one is open.
 *
 * <p>Reads the shop's name, the section it names and the operator's logout, and tells whether a
 * shopper's entry stands in it.
 */
public class BackofficeHeader extends BasePage {

  private static final String SITE_HEADER = "site-header";
  private static final String LOGO = "site-logo";
  private static final String SECTION = "backoffice-section";
  private static final String LOGOUT_BUTTON = "backoffice-logout-button";

  /**
   * Creates the header of the back-office page that is open and waits for it.
   *
   * @param page the Playwright page instance
   */
  public BackofficeHeader(Page page) {
    super(page);
    waitFor(SITE_HEADER);
  }

  /**
   * The shop's name as the logo shows it.
   *
   * @return the logo text, without whitespace between its parts
   */
  public String shopName() {
    return textOf(LOGO).replaceAll("\\s+", "");
  }

  /**
   * The section the header names.
   *
   * @return its text, or empty when the header names none
   */
  public String section() {
    return textOf(SECTION);
  }

  /**
   * The text of the operator's logout button.
   *
   * @return the button text, or empty when the header offers none
   */
  public String logoutButton() {
    return textOf(LOGOUT_BUTTON);
  }

  /**
   * Whether the header offers a link or a button of the given name.
   *
   * @param name the visible name, matched exactly
   * @return true when the header shows one
   */
  public boolean offers(String name) {
    Locator header = header();
    return header.getByRole(AriaRole.LINK, exactly(name)).count()
            + header.getByRole(AriaRole.BUTTON, exactly(name)).count()
        > 0;
  }

  private String textOf(String dataTest) {
    Locator element = header().locator("[data-test='" + dataTest + "']");
    return element.count() == 0 ? "" : element.first().innerText().strip();
  }

  private Locator header() {
    return page.locator("[data-test='" + SITE_HEADER + "']");
  }

  private static Locator.GetByRoleOptions exactly(String name) {
    return new Locator.GetByRoleOptions().setName(name).setExact(true);
  }
}
