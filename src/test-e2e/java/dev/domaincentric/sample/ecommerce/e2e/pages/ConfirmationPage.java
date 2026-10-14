package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the order confirmation page.
 *
 * <p>Provides methods to verify the order was successfully placed.
 */
public class ConfirmationPage extends BasePage {

  private static final String URL_PATTERN = "/checkout/confirmation";
  private static final String CONFIRMATION_MESSAGE = "confirmation-message";

  /**
   * Creates a new ConfirmationPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public ConfirmationPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Creates this page of a shop the test started itself and waits for it to load.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   */
  public ConfirmationPage(Page page, String shopUrl) {
    super(page, shopUrl, URL_PATTERN);
  }

  /**
   * Checks if the order confirmation message is displayed.
   *
   * @return true if the confirmation message is visible
   */
  public boolean isOrderConfirmed() {
    return exists(CONFIRMATION_MESSAGE);
  }
}
