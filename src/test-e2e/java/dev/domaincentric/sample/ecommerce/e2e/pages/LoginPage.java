package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the login page.
 *
 * <p>Provides methods to authenticate users.
 */
public class LoginPage extends BasePage {

  private static final String URL_PATTERN = "/login";
  private static final String SUBMIT_BUTTON = "login-submit-button";
  private static final String REGISTER_LINK = "login-register-link";
  private static final String ERROR_MESSAGE = "login-error-message";
  private static final String EMAIL_INPUT = "login-email-input";
  private static final String PASSWORD_INPUT = "login-password-input";

  /**
   * Creates a new LoginPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public LoginPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates to the login page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new LoginPage
   */
  public static LoginPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new LoginPage(page);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public LoginPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the password field.
   *
   * @param password the password
   * @return this page for method chaining
   */
  public LoginPage fillPassword(String password) {
    fill("password", password);
    return this;
  }

  /**
   * Fills login credentials.
   *
   * @param email the email address
   * @param password the password
   * @return this page for method chaining
   */
  public LoginPage fillCredentials(String email, String password) {
    return fillEmail(email).fillPassword(password);
  }

  /**
   * Submits the login form. After successful login, caller should create the appropriate page
   * object.
   */
  public void submit() {
    click(SUBMIT_BUTTON);
    page.waitForURL(url -> !url.contains("/login"));
  }

  /**
   * Submits the login form and waits for the login page to come back with its error message.
   *
   * <p>Use this when expecting the login to be refused.
   *
   * @return the login page as it is shown again
   */
  public LoginPage submitExpectingError() {
    click(SUBMIT_BUTTON);
    waitFor(ERROR_MESSAGE);
    return new LoginPage(page);
  }

  /**
   * The error message the login page shows.
   *
   * @return the message text, trimmed
   */
  public String errorMessage() {
    return page.locator("[data-test='" + ERROR_MESSAGE + "']").textContent().strip();
  }

  /**
   * What the Email field holds.
   *
   * @return the field's current value
   */
  public String emailValue() {
    return page.locator("[data-test='" + EMAIL_INPUT + "']").inputValue();
  }

  /**
   * What the Password field holds.
   *
   * @return the field's current value
   */
  public String passwordValue() {
    return page.locator("[data-test='" + PASSWORD_INPUT + "']").inputValue();
  }

  /**
   * Performs login with the given credentials.
   *
   * @param email the email address
   * @param password the password
   */
  public void login(String email, String password) {
    fillCredentials(email, password);
    submit();
  }

  /**
   * Navigates to the registration page.
   *
   * @return the RegisterPage
   */
  public RegisterPage goToRegister() {
    click(REGISTER_LINK);
    return new RegisterPage(page);
  }
}
