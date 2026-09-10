package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the buyer information page in checkout.
 *
 * <p>Provides methods to fill buyer information and navigate to login/register for authenticated
 * checkout.
 */
public class BuyerInfoPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/buyer";
  private static final String CONTINUE_BUTTON = "buyer-continue-button";
  private static final String LOGIN_LINK = "buyer-login-link";
  private static final String REGISTER_LINK = "buyer-register-link";
  private static final String ERROR_MESSAGE = "buyer-error-message";
  private static final String AUTH_OPTIONS = "buyer-auth-options";
  private static final String LOGGED_IN_BANNER = "buyer-logged-in";
  private static final String EMAIL_INPUT = "buyer-email-input";

  /**
   * Creates a new BuyerInfoPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public BuyerInfoPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Fills the email field.
   *
   * @param email the email address
   * @return this page for method chaining
   */
  public BuyerInfoPage fillEmail(String email) {
    fill("email", email);
    return this;
  }

  /**
   * Fills the first name field.
   *
   * @param firstName the first name
   * @return this page for method chaining
   */
  public BuyerInfoPage fillFirstName(String firstName) {
    fill("firstName", firstName);
    return this;
  }

  /**
   * Fills the last name field.
   *
   * @param lastName the last name
   * @return this page for method chaining
   */
  public BuyerInfoPage fillLastName(String lastName) {
    fill("lastName", lastName);
    return this;
  }

  /**
   * Fills the phone number field.
   *
   * @param phone the phone number
   * @return this page for method chaining
   */
  public BuyerInfoPage fillPhone(String phone) {
    fill("phone", phone);
    return this;
  }

  /**
   * Fills all buyer information fields at once.
   *
   * @param email the email address
   * @param firstName the first name
   * @param lastName the last name
   * @param phone the phone number
   * @return this page for method chaining
   */
  public BuyerInfoPage fillBuyerInfo(
      String email, String firstName, String lastName, String phone) {
    return fillEmail(email).fillFirstName(firstName).fillLastName(lastName).fillPhone(phone);
  }

  /**
   * Clicks the continue button to proceed to delivery.
   *
   * @return the DeliveryPage
   */
  public DeliveryPage continueToDelivery() {
    click(CONTINUE_BUTTON);
    return new DeliveryPage(page);
  }

  /**
   * Clicks the continue button, expecting to stay on the same page due to validation errors.
   *
   * <p>Handles both HTML5 client-side validation (which prevents form submission) and server-side
   * validation (which redirects back with error flash attributes).
   *
   * @return this page for method chaining
   */
  public BuyerInfoPage submitWithErrors() {
    click(CONTINUE_BUTTON);
    // Brief wait: HTML5 validation prevents submission (no navigation),
    // server-side validation triggers a redirect back to this page.
    page.waitForTimeout(500);
    return this;
  }

  /**
   * Navigates to the login page from checkout.
   *
   * @return the LoginPage
   */
  public LoginPage goToLogin() {
    click(LOGIN_LINK);
    return new LoginPage(page);
  }

  /**
   * Navigates to the register page from checkout.
   *
   * @return the RegisterPage
   */
  public RegisterPage goToRegister() {
    click(REGISTER_LINK);
    return new RegisterPage(page);
  }

  /**
   * Checks if an error message is displayed.
   *
   * @return true if error message exists
   */
  public boolean hasErrorMessage() {
    return exists(ERROR_MESSAGE);
  }

  /**
   * Checks if the page contains validation error text or HTML5 validation failures.
   *
   * <p>Detects three types of validation:
   *
   * <ul>
   *   <li>Server-side errors rendered via data-test="buyer-error-message"
   *   <li>Server-side error text in the page body
   *   <li>HTML5 constraint validation on the buyer form inputs
   * </ul>
   *
   * @return true if validation errors are shown
   */
  public boolean hasValidationErrors() {
    return hasErrorMessage()
        || pageContains("valid email")
        || pageContains("error")
        || hasHtml5ValidationErrors();
  }

  private boolean hasHtml5ValidationErrors() {
    return (boolean)
        page.evaluate(
            "() => { const form = document.querySelector('[data-test=\"buyer-form\"]'); "
                + "return form != null && !form.checkValidity(); }");
  }

  /**
   * Checks if the login/register prompt is offered. It is shown to anonymous visitors only.
   *
   * @return true if the guest login/register options are rendered
   */
  public boolean showsAuthOptions() {
    return exists(AUTH_OPTIONS);
  }

  /**
   * Reads the "Logged in as ..." banner shown instead of the login/register prompt.
   *
   * @return the banner text, or an empty string when the visitor is anonymous
   */
  public String loggedInBannerText() {
    if (!exists(LOGGED_IN_BANNER)) {
      return "";
    }
    return page.locator("[data-test='" + LOGGED_IN_BANNER + "']").textContent().trim();
  }

  /**
   * Reads the current value of the email input.
   *
   * @return the prefilled email address, empty when the field is blank
   */
  public String emailValue() {
    return page.locator("[data-test='" + EMAIL_INPUT + "']").inputValue();
  }

  /**
   * Checks if the current URL indicates we're still on the buyer info page.
   *
   * @return true if still on buyer info page
   */
  /**
   * Reads the checkout session's order summary as {@code name x quantity} pairs — the snapshot the
   * session was started from, independent of the cart's current contents.
   *
   * @return one entry per summary line, e.g. {@code "DDD Pattern Card Deck x2"}
   */
  public java.util.List<String> summaryItems() {
    java.util.List<String> lines = new java.util.ArrayList<>();
    var items = page.locator("[data-test='order-summary-item']");
    for (int i = 0; i < items.count(); i++) {
      var item = items.nth(i);
      lines.add(
          item.locator("[data-test='order-summary-item-name']").textContent().trim()
              + " "
              + item.locator("[data-test='order-summary-item-qty']").textContent().trim());
    }
    return lines;
  }

  public boolean isOnPage() {
    return getCurrentPath().contains("/checkout/buyer");
  }
}
