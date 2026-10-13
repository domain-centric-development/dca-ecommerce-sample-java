package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for "My Profile", the account page that changes the email address and the date of
 * birth.
 *
 * <p>Fills and saves the form and reads the fields and the message the page shows afterwards.
 */
public class ProfilePage extends BasePage {

  private static final String URL_PATTERN = "/account/profile";
  private static final String PROFILE = "profile-page";
  private static final String EMAIL_INPUT = "profile-email-input";
  private static final String DATE_OF_BIRTH_INPUT = "profile-date-of-birth-input";
  private static final String SUBMIT_BUTTON = "profile-submit-button";
  private static final String SUCCESS_MESSAGE = "profile-success-message";

  /**
   * Creates the profile page and waits for it.
   *
   * @param page the Playwright page instance
   */
  public ProfilePage(Page page) {
    super(page, URL_PATTERN);
    waitFor(PROFILE);
  }

  /**
   * Opens the profile page.
   *
   * @param page the Playwright page instance of a logged-in customer
   * @return the profile page
   */
  public static ProfilePage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new ProfilePage(page);
  }

  /**
   * Fills both fields of the form.
   *
   * @param email the email address to enter
   * @param dateOfBirth the date of birth to enter, as yyyy-MM-dd
   * @return this page
   */
  public ProfilePage fillDetails(String email, String dateOfBirth) {
    input(EMAIL_INPUT).fill(email);
    input(DATE_OF_BIRTH_INPUT).fill(dateOfBirth);
    return this;
  }

  /**
   * Presses "Save Changes" and waits for the page to come back with its success message.
   *
   * @return the profile page as it is shown again
   */
  public ProfilePage saveExpectingSuccess() {
    click(SUBMIT_BUTTON);
    waitFor(SUCCESS_MESSAGE);
    return new ProfilePage(page);
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
   * The success message the page shows.
   *
   * @return the message text, trimmed
   */
  public String successMessage() {
    return page.locator("[data-test='" + SUCCESS_MESSAGE + "']").innerText().strip();
  }

  /**
   * The value the email field holds.
   *
   * @return the field's value
   */
  public String emailValue() {
    return input(EMAIL_INPUT).inputValue();
  }

  /**
   * The value the date of birth field holds.
   *
   * @return the field's value, as yyyy-MM-dd
   */
  public String dateOfBirthValue() {
    return input(DATE_OF_BIRTH_INPUT).inputValue();
  }

  private Locator input(String dataTest) {
    return page.locator("[data-test='" + dataTest + "']");
  }
}
