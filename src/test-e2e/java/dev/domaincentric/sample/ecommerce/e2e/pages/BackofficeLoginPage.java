package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the backoffice login page.
 *
 * <p>Provides methods to authenticate backoffice users and verify login state.
 */
public class BackofficeLoginPage extends BasePage {

  private static final String URL_PATTERN = "/backoffice/login**";
  private static final String SUBMIT_BUTTON = "backoffice-login-submit";
  private static final String LOGIN_ERROR_MESSAGE = "login-error-message";

  /**
   * Creates a new BackofficeLoginPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public BackofficeLoginPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates to the backoffice login page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new BackofficeLoginPage
   */
  public static BackofficeLoginPage navigateTo(Page page) {
    page.navigate(BASE_URL + "/backoffice/login");
    return new BackofficeLoginPage(page);
  }

  /**
   * Fills the username and password fields.
   *
   * @param username the backoffice username
   * @param password the backoffice password
   * @return this page for method chaining
   */
  public BackofficeLoginPage fillCredentials(String username, String password) {
    fill("username", username);
    fill("password", password);
    return this;
  }

  /**
   * Submits the login form and waits for a successful redirect to the events page.
   *
   * @return the BackofficeEventsPage after successful login
   */
  public BackofficeEventsPage submit() {
    click(SUBMIT_BUTTON);
    page.waitForURL(url -> !url.contains("/login"));
    return new BackofficeEventsPage(page);
  }

  /**
   * Submits the login form and waits for the error message to appear.
   *
   * <p>Use this when expecting login to fail due to invalid credentials.
   */
  public void submitExpectingError() {
    click(SUBMIT_BUTTON);
    waitFor(LOGIN_ERROR_MESSAGE);
  }
}
