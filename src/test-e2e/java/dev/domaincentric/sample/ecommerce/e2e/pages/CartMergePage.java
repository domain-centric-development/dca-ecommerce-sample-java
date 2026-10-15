package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the cart merge options page.
 *
 * <p>Provides methods to interact with the cart merge decision flow when a user logs in and both
 * carts have items.
 */
public class CartMergePage extends BasePage {

  private static final String URL_PATTERN = "/cart/merge**";
  private static final String MERGE_OPTION_MERGE_BOTH = "merge-option-merge-both";
  private static final String MERGE_SUBMIT_BUTTON = "merge-submit-button";

  /**
   * Creates a new CartMergePage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public CartMergePage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Creates a CartMergePage without URL validation. Use this when checking if we're redirected to
   * merge page.
   *
   * @param page the Playwright page instance
   * @param skipValidation ignored, just differentiates constructors
   */
  public CartMergePage(Page page, boolean skipValidation) {
    super(page);
  }

  /**
   * Checks if currently on the cart merge page.
   *
   * @return true if on the merge page
   */
  public boolean isOnMergePage() {
    return getCurrentPath().startsWith("/cart/merge");
  }

  /**
   * Selects the "Merge Both Carts" option.
   *
   * @return this page for method chaining
   */
  public CartMergePage selectMergeBoth() {
    click(MERGE_OPTION_MERGE_BOTH);
    return this;
  }

  /**
   * Submits the merge decision and navigates to cart page.
   *
   * @return the CartPage after merge
   */
  public CartPage submitMerge() {
    click(MERGE_SUBMIT_BUTTON);
    page.waitForURL(BASE_URL + "/cart");
    return new CartPage(page);
  }

  /**
   * Selects merge option and submits in one step.
   *
   * @return the CartPage after merge
   */
  public CartPage mergeBothCarts() {
    selectMergeBoth();
    return submitMerge();
  }
}
