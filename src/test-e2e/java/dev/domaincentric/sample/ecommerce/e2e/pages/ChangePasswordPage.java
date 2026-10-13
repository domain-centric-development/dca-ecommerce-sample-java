package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the change-password page of the account area.
 *
 * <p>Fills and submits the form, and reads the confirmation and what the fields hold.
 */
public class ChangePasswordPage extends BasePage {

  private static final String URL_PATTERN = "/account/change-password";
  private static final String PAGE = "change-password-page";
  private static final String CURRENT_PASSWORD_INPUT = "change-password-current-password-input";
  private static final String NEW_PASSWORD_INPUT = "change-password-new-password-input";
  private static final String CONFIRM_PASSWORD_INPUT = "change-password-confirm-password-input";
  private static final String SUBMIT_BUTTON = "change-password-submit-button";
  private static final String SUCCESS_MESSAGE = "change-password-success-message";
  private static final String ERROR_MESSAGE = "change-password-error-message";

  /**
   * Creates the change-password page and waits for it.
   *
   * @param page the Playwright page instance
   */
  public ChangePasswordPage(Page page) {
    super(page, URL_PATTERN);
    waitFor(PAGE);
  }

  /**
   * Navigates to the change-password page of the logged-in customer.
   *
   * @param page the Playwright page instance
   * @return the change-password page
   */
  public static ChangePasswordPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new ChangePasswordPage(page);
  }

  /**
   * Fills the three fields and submits the form, expecting the change to be confirmed.
   *
   * @param currentPassword the current password
   * @param newPassword the new password
   * @param confirmPassword the confirmation of the new password
   * @return the change-password page as it opens again, its confirmation shown
   */
  public ChangePasswordPage changePassword(
      String currentPassword, String newPassword, String confirmPassword) {
    fill("currentPassword", currentPassword);
    fill("newPassword", newPassword);
    fill("confirmPassword", confirmPassword);
    click(SUBMIT_BUTTON);
    waitFor(SUCCESS_MESSAGE);
    return new ChangePasswordPage(page);
  }

  /**
   * Fills the three fields and submits the form, expecting the change to be refused.
   *
   * @param currentPassword the current password
   * @param newPassword the new password
   * @param confirmPassword the confirmation of the new password
   * @return the change-password page as it is shown again, its refusal shown
   */
  public ChangePasswordPage submitRefused(
      String currentPassword, String newPassword, String confirmPassword) {
    fill("currentPassword", currentPassword);
    fill("newPassword", newPassword);
    fill("confirmPassword", confirmPassword);
    click(SUBMIT_BUTTON);
    waitFor(ERROR_MESSAGE);
    return new ChangePasswordPage(page);
  }

  /**
   * The refusal the page shows.
   *
   * @return the message text, trimmed
   */
  public String errorMessage() {
    return page.locator("[data-test='" + ERROR_MESSAGE + "']").innerText().strip();
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
   * The confirmation the page shows.
   *
   * @return the message text, trimmed
   */
  public String successMessage() {
    return page.locator("[data-test='" + SUCCESS_MESSAGE + "']").innerText().strip();
  }

  /**
   * What the Current Password field holds.
   *
   * @return the field's current value
   */
  public String currentPasswordValue() {
    return valueOf(CURRENT_PASSWORD_INPUT);
  }

  /**
   * What the New Password field holds.
   *
   * @return the field's current value
   */
  public String newPasswordValue() {
    return valueOf(NEW_PASSWORD_INPUT);
  }

  /**
   * What the Confirm New Password field holds.
   *
   * @return the field's current value
   */
  public String confirmPasswordValue() {
    return valueOf(CONFIRM_PASSWORD_INPUT);
  }

  private String valueOf(String dataTest) {
    return page.locator("[data-test='" + dataTest + "']").inputValue();
  }
}
