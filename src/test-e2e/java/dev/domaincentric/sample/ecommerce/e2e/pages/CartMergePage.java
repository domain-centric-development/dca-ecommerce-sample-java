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
  private static final String ANONYMOUS_CART_SUMMARY = "anonymous-cart-summary";
  private static final String ACCOUNT_CART_SUMMARY = "account-cart-summary";
  private static final String MERGE_OPTION_MERGE_BOTH = "merge-option-merge-both";
  private static final String MERGE_OPTION_USE_ACCOUNT = "merge-option-use-account";
  private static final String MERGE_OPTION_USE_ANONYMOUS = "merge-option-use-anonymous";
  private static final String MERGE_SUBMIT_BUTTON = "merge-submit-button";
  private static final String MERGE_FORM = "merge-form";

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
   * Checks if anonymous cart summary is displayed.
   *
   * @return true if anonymous cart is shown
   */
  public boolean showsAnonymousCart() {
    return exists(ANONYMOUS_CART_SUMMARY);
  }

  /**
   * Checks if account cart summary is displayed.
   *
   * @return true if account cart is shown
   */
  public boolean showsAccountCart() {
    return exists(ACCOUNT_CART_SUMMARY);
  }

  /**
   * Checks if all three merge options are displayed.
   *
   * @return true if all options are shown
   */
  public boolean showsMergeOptions() {
    return exists(MERGE_OPTION_MERGE_BOTH)
        && exists(MERGE_OPTION_USE_ACCOUNT)
        && exists(MERGE_OPTION_USE_ANONYMOUS);
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
   * Selects the "Use Account Cart Only" option.
   *
   * @return this page for method chaining
   */
  public CartMergePage selectUseAccountCart() {
    click(MERGE_OPTION_USE_ACCOUNT);
    return this;
  }

  /**
   * Selects the "Use Anonymous Cart Only" option.
   *
   * @return this page for method chaining
   */
  public CartMergePage selectUseAnonymousCart() {
    click(MERGE_OPTION_USE_ANONYMOUS);
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

  /**
   * Selects account cart option and submits in one step.
   *
   * @return the CartPage after merge
   */
  public CartPage useAccountCartOnly() {
    selectUseAccountCart();
    return submitMerge();
  }

  /**
   * Selects anonymous cart option and submits in one step.
   *
   * @return the CartPage after merge
   */
  public CartPage useAnonymousCartOnly() {
    selectUseAnonymousCart();
    return submitMerge();
  }
}
