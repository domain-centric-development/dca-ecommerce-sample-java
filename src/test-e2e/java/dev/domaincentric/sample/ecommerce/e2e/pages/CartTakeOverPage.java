package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page object for the page after the login that takes the guest cart over: a browser that runs
 * scripts sends its form by itself, any other shows it with a "Continue" button.
 */
public class CartTakeOverPage extends BasePage {

  private static final String MESSAGE = "take-over-message";
  private static final String CONTINUE_BUTTON = "take-over-continue-button";

  /**
   * Creates the page object without URL validation: the test asserts the page's message first, so a
   * login that leads elsewhere fails on that expectation.
   *
   * @param page the Playwright page instance
   */
  public CartTakeOverPage(Page page) {
    super(page);
  }

  /**
   * The sentence the page shows.
   *
   * @return the message's locator
   */
  public Locator message() {
    return page.locator("[data-test='" + MESSAGE + "']");
  }

  /**
   * Presses "Continue" and waits for the cart page.
   *
   * @return the cart page the take-over leads to
   */
  public CartPage continueToCart() {
    click(CONTINUE_BUTTON);
    return new CartPage(page);
  }
}
