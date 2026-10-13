package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for the account overview, the first page of the account area.
 *
 * <p>Reads the greeting and the details the "Account Overview" card shows.
 */
public class AccountOverviewPage extends BasePage {

  private static final String URL_PATTERN = "/account";
  private static final String OVERVIEW = "account-overview";
  private static final String GREETING = "account-greeting";
  private static final String EMAIL = "account-email";
  private static final String LAST_LOGIN = "account-last-login";
  // The card's title and the detail labels carry no data-test; adopting the page changes no
  // template.
  private static final String CARD_TITLE = ".card__header-title";
  private static final String DETAIL_ROW = ".account__detail-row";
  private static final String DETAIL_LABEL = ".account__detail-label";

  /**
   * Creates the overview page and waits for it.
   *
   * @param page the Playwright page instance
   */
  public AccountOverviewPage(Page page) {
    super(page, URL_PATTERN);
    waitFor(OVERVIEW);
  }

  /**
   * The path the browser shows.
   *
   * @return the current path, without the base URL
   */
  public String shownPath() {
    return getCurrentPath();
  }

  /**
   * The page's heading.
   *
   * @return the greeting text, trimmed
   */
  public String greeting() {
    return text(GREETING);
  }

  /**
   * The title of the card the account's details are shown in.
   *
   * @return the card title, trimmed
   */
  public String cardTitle() {
    return page.locator("[data-test='" + OVERVIEW + "'] " + CARD_TITLE).innerText().strip();
  }

  /**
   * The label of the detail row that holds the email address.
   *
   * @return the label text, trimmed
   */
  public String emailLabel() {
    return labelOf(EMAIL);
  }

  /**
   * The email address the card shows.
   *
   * @return the address, trimmed
   */
  public String email() {
    return text(EMAIL);
  }

  /**
   * The label of the detail row that holds the last login.
   *
   * @return the label text, trimmed
   */
  public String lastLoginLabel() {
    return labelOf(LAST_LOGIN);
  }

  /**
   * The last login the card shows.
   *
   * @return the last login as displayed, trimmed
   */
  public String lastLogin() {
    return text(LAST_LOGIN);
  }

  private String labelOf(String valueDataTest) {
    return page.locator(DETAIL_ROW)
        .filter(
            new Locator.FilterOptions().setHas(page.locator("[data-test='" + valueDataTest + "']")))
        .locator(DETAIL_LABEL)
        .innerText()
        .strip();
  }

  private String text(String dataTest) {
    return page.locator("[data-test='" + dataTest + "']").innerText().strip();
  }
}
