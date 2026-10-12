package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the registration page.
 *
 * <p>Provides methods to register new users.
 */
public class RegisterPage extends BasePage {

  private static final String URL_PATTERN = "/register";

  /** Registration requires an owner; flows that only care about credentials use these. */
  private static final String DEFAULT_FIRST_NAME = "Test";

  private static final String DEFAULT_LAST_NAME = "User";

  private static final String DEFAULT_DATE_OF_BIRTH = "1990-05-17";
  private static final String SUBMIT_BUTTON = "register-submit-button";
  private static final String CONFIRM_PASSWORD_INPUT = "register-confirm-password-input";
  private static final String ERROR_MESSAGE = "register-error-message";

  /**
   * Creates a new RegisterPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public RegisterPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Navigates to the register page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new RegisterPage
   */
  public static RegisterPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new RegisterPage(page);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public RegisterPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the password field.
   *
   * @param password the password
   * @return this page for method chaining
   */
  public RegisterPage fillPassword(String password) {
    fill("password", password);
    return this;
  }

  /**
   * Fills the confirm password field.
   *
   * @param password the password to confirm
   * @return this page for method chaining
   */
  public RegisterPage fillConfirmPassword(String password) {
    fill("confirmPassword", password);
    return this;
  }

  /**
   * Fills the owner fields.
   *
   * @param firstName the owner's first name
   * @param lastName the owner's last name
   * @param dateOfBirth the owner's date of birth, in ISO format
   * @return this page for method chaining
   */
  public RegisterPage fillOwner(String firstName, String lastName, String dateOfBirth) {
    fill("firstName", firstName);
    fill("lastName", lastName);
    fill("dateOfBirth", dateOfBirth);
    return this;
  }

  /**
   * Fills all registration fields, using the default owner.
   *
   * @param email the email address
   * @param password the password
   * @return this page for method chaining
   */
  public RegisterPage fillRegistrationForm(String email, String password) {
    return fillOwner(DEFAULT_FIRST_NAME, DEFAULT_LAST_NAME, DEFAULT_DATE_OF_BIRTH)
        .fillEmail(email)
        .fillPassword(password)
        .fillConfirmPassword(password);
  }

  /**
   * Submits the registration form. After successful registration, caller should create the
   * appropriate page object.
   */
  public void submit() {
    click(SUBMIT_BUTTON);
    page.waitForURL(url -> !url.contains("/register"));
  }

  /**
   * Submits a registration the shop refuses, and waits for the form to come back with its message.
   *
   * @return this page, re-rendered with the refusal
   */
  public RegisterPage submitExpectingRefusal() {
    click(SUBMIT_BUTTON);
    waitFor(ERROR_MESSAGE);
    return this;
  }

  /**
   * The refusal the page shows above the form.
   *
   * @return the message text
   */
  public String errorMessage() {
    return page.locator("[data-test='" + ERROR_MESSAGE + "']").textContent().strip();
  }

  /**
   * Submits a registration form the shop refuses; the registration page opens again with the
   * refusal.
   *
   * @return the refusal the page shows
   */
  public String submitRefused() {
    click(SUBMIT_BUTTON);
    waitFor(ERROR_MESSAGE);
    return page.locator("[data-test='" + ERROR_MESSAGE + "']").textContent().strip();
  }

  /**
   * The value a form field holds as the page shows it.
   *
   * @param name the name attribute of the input
   * @return the field's current value
   */
  public String fieldValue(String name) {
    return page.locator("input[name='" + name + "']").inputValue();
  }

  /**
   * Performs registration with the given credentials.
   *
   * @param email the email address
   * @param password the password
   */
  public void register(String email, String password) {
    fillRegistrationForm(email, password);
    submit();
  }

  /**
   * Checks if confirm password field exists.
   *
   * @return true if confirm password is required
   */
  public boolean requiresConfirmPassword() {
    return exists(CONFIRM_PASSWORD_INPUT);
  }
}
