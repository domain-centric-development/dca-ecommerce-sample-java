package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for the order review page in checkout.
 *
 * <p>Provides methods to verify order details and place the order.
 */
public class ReviewPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/review";
  private static final String PLACE_ORDER_BUTTON = "review-place-order-button";
  private static final String TOTAL = "review-total";
  private static final String ERROR_MESSAGE = "review-error-message";
  private static final String NOTICE_MESSAGE = "review-notice-message";
  private static final String BACK_LINK = "review-back-link";

  /**
   * Creates a new ReviewPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public ReviewPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Creates this page of a shop the test started itself and waits for it to load.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   */
  public ReviewPage(Page page, String shopUrl) {
    super(page, shopUrl, URL_PATTERN);
  }

  /**
   * Checks if the page displays the expected email.
   *
   * @param email the expected email address
   * @return true if the email is displayed
   */
  public boolean showsEmail(String email) {
    return pageContains(email);
  }

  /**
   * Reads the checkout session's total as the page shows it.
   *
   * @return the amount and the currency code, e.g. {@code "45.98 EUR"}
   */
  public String total() {
    return page.locator("[data-test='" + TOTAL + "']").textContent().trim();
  }

  /**
   * The error message the review step shows, for assertions on it.
   *
   * @return the message's locator
   */
  public Locator errorMessage() {
    return page.locator("[data-test='" + ERROR_MESSAGE + "']");
  }

  /**
   * The notice the review step shows, for assertions on it.
   *
   * @return the notice's locator
   */
  public Locator notice() {
    return page.locator("[data-test='" + NOTICE_MESSAGE + "']");
  }

  /**
   * Follows the "Back" link to the payment step.
   *
   * @return the PaymentPage
   */
  public PaymentPage backToPayment() {
    click(BACK_LINK);
    return new PaymentPage(page, shopUrl);
  }

  /** Presses "Place Order" without assuming where the shop sends the guest. */
  public void pressPlaceOrder() {
    click(PLACE_ORDER_BUTTON);
  }

  /**
   * Clicks the place order button to complete the purchase.
   *
   * @return the ConfirmationPage
   */
  public ConfirmationPage placeOrder() {
    click(PLACE_ORDER_BUTTON);
    return new ConfirmationPage(page, shopUrl);
  }
}
