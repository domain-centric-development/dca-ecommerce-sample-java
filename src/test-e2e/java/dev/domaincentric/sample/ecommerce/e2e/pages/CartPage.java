package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page object for the shopping cart page.
 *
 * <p>Provides methods to view cart contents and proceed to checkout.
 */
public class CartPage extends BasePage {

  // The cart page itself, not a page below it: the page after the login (/cart/merge) is passed
  // on the way.
  private static final String URL_PATTERN = "/cart";
  private static final String CART_ITEM = "cart-item";
  private static final String CHECKOUT_BUTTON = "cart-checkout-button";
  private static final String SUCCESS_MESSAGE = "cart-success-message";
  private static final String ERROR_MESSAGE = "cart-error-message";
  private static final String REMOVE_BUTTON = "cart-item-remove-button";

  /**
   * Creates a new CartPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public CartPage(Page page) {
    super(page, URL_PATTERN);
  }

  /**
   * Creates the cart page of a shop the test started itself and waits for it to load.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   */
  public CartPage(Page page, String shopUrl) {
    super(page, shopUrl, URL_PATTERN);
  }

  /**
   * Navigates to the cart and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new CartPage
   */
  public static CartPage navigateTo(Page page) {
    page.navigate(BASE_URL + "/cart");
    return new CartPage(page);
  }

  /**
   * Navigates to the cart of a shop the test started itself.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   * @return a new CartPage
   */
  public static CartPage navigateTo(Page page, String shopUrl) {
    page.navigate(shopUrl + "/cart");
    return new CartPage(page, shopUrl);
  }

  /**
   * Waits for cart items to be displayed and proceeds to checkout.
   *
   * @return the BuyerInfoPage to enter buyer information
   */
  public BuyerInfoPage proceedToCheckout() {
    waitFor(CART_ITEM);
    click(CHECKOUT_BUTTON);
    return new BuyerInfoPage(page, shopUrl);
  }

  /**
   * Proceeds to checkout and walks every step to the review as Jane Doe: her buyer details, her
   * address in Berlin, "Standard Shipping" and the offered payment provider.
   *
   * @return the ReviewPage
   */
  public ReviewPage proceedToTheReviewAsJaneDoe() {
    return proceedToTheReviewWithBuyerEmail("jane@example.com");
  }

  /**
   * Proceeds to checkout and walks every step to the review as Jane Doe, with that buyer e-mail.
   *
   * @param buyerEmail the e-mail entered at the buyer step
   * @return the ReviewPage
   */
  public ReviewPage proceedToTheReviewWithBuyerEmail(String buyerEmail) {
    return proceedToCheckout()
        .fillBuyerInfo(buyerEmail, "Jane", "Doe", "+49 30 1234567")
        .continueToDelivery()
        .fillStreet("Hauptstrasse 1")
        .fillCity("Berlin")
        .fillPostalCode("10115")
        .fillCountry("Germany")
        .chooseShippingMethod("Standard Shipping")
        .continueToPayment()
        .chooseTheOfferedProvider()
        .continueToReview();
  }

  /**
   * Presses "Proceed to Checkout" without expecting where the shop leads — the caller asserts the
   * page it lands on.
   */
  public void pressProceedToCheckout() {
    waitFor(CART_ITEM);
    click(CHECKOUT_BUTTON);
  }

  /**
   * Checks if the cart has at least one item.
   *
   * @return true if cart contains items
   */
  public boolean hasItems() {
    return exists(CART_ITEM);
  }

  /**
   * Gets the number of items in the cart.
   *
   * @return the count of cart items
   */
  public int getItemCount() {
    return page.locator("[data-test='" + CART_ITEM + "']").count();
  }

  /**
   * Reads the cart lines as {@code name x quantity} pairs, in display order.
   *
   * @return one entry per cart line, e.g. {@code "DDD Pattern Card Deck x2"}
   */
  public java.util.List<String> lineItems() {
    java.util.List<String> lines = new java.util.ArrayList<>();
    var items = page.locator("[data-test='" + CART_ITEM + "']");
    for (int i = 0; i < items.count(); i++) {
      var item = items.nth(i);
      lines.add(
          item.locator("[data-test='cart-item-name']").textContent().trim()
              + " x"
              + item.locator("[data-test='cart-item-quantity']").textContent().trim());
    }
    return lines;
  }

  /**
   * The value a cart line shows under a label, such as "Unit Price".
   *
   * @param productName the name the line shows
   * @param label the label as the page shows it
   * @return the value text, trimmed
   */
  public String lineValue(String productName, String label) {
    return lineOf(productName)
        .locator(".cart__item-row")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator(".cart__item-label", new Page.LocatorOptions().setHasText(label))))
        .locator("span")
        .last()
        .textContent()
        .strip();
  }

  /**
   * Whether a cart line shows a product image the browser loaded.
   *
   * @param productName the name the line shows
   * @return true if the line's image is visible and loaded
   */
  public boolean showsImageOf(String productName) {
    Locator image = lineOf(productName).locator(".cart__item-image img");
    return image.count() == 1
        && image.isVisible()
        && (Boolean) image.evaluate("e => e.complete && e.naturalWidth > 0");
  }

  /**
   * The stock warning a cart line shows, once it shows one — the page reloaded until it does or the
   * time is up, for stock the shop lowers shortly after an order placed elsewhere.
   *
   * @param productName the name the line shows
   * @param within how long to wait for the warning
   * @return the warning's text, trimmed; empty when the line shows none
   */
  public String stockWarningOnceShown(String productName, java.time.Duration within) {
    final long deadline = System.nanoTime() + within.toNanos();
    Locator warning = lineOf(productName).locator("[data-test='stock-alert']");
    while (warning.count() == 0 && System.nanoTime() < deadline) {
      page.reload();
    }
    return warning.count() == 0 ? "" : warning.textContent().strip();
  }

  /**
   * The names of the cart lines, in display order.
   *
   * @return one name per cart line
   */
  public java.util.List<String> lineNames() {
    return page
        .locator("[data-test='" + CART_ITEM + "'] [data-test='cart-item-name']")
        .allTextContents()
        .stream()
        .map(String::strip)
        .toList();
  }

  /**
   * The note a cart line shows when its price changed since it was added.
   *
   * @param productName the name the line shows
   * @return the note's locator
   */
  public Locator priceChangeOf(String productName) {
    return lineOf(productName).locator("[data-test='price-change-alert']");
  }

  /**
   * The note above the lines that some prices changed since they were added.
   *
   * @return the note's locator
   */
  public Locator priceChangeSummary() {
    return page.locator("[data-test='price-change-summary']");
  }

  /**
   * The "Remove" button of a cart line.
   *
   * @param productName the name the line shows
   * @return the button's locator
   */
  public Locator removeButtonOf(String productName) {
    return lineOf(productName).locator("[data-test='" + REMOVE_BUTTON + "']");
  }

  /**
   * Presses "Remove" on a cart line and waits for the cart page without that line.
   *
   * @param productName the name the line shows
   * @return the cart page the shop leads back to
   */
  public CartPage remove(String productName) {
    removeButtonOf(productName).click();
    lineOf(productName)
        .waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.DETACHED));
    return new CartPage(page, shopUrl);
  }

  private Locator lineOf(String productName) {
    return page.locator("[data-test='" + CART_ITEM + "']")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator(
                        "[data-test='cart-item-name']",
                        new Page.LocatorOptions().setHasText(productName))));
  }

  /**
   * The success message the cart page shows after a product was added.
   *
   * @return the message text, trimmed
   */
  public String successMessage() {
    return page.locator("[data-test='" + SUCCESS_MESSAGE + "']").textContent().strip();
  }

  /**
   * The error message the cart page shows, for assertions on its text.
   *
   * @return the message's locator
   */
  public Locator errorMessage() {
    return page.locator("[data-test='" + ERROR_MESSAGE + "']");
  }

  /**
   * The path of the page the browser shows.
   *
   * @return the path, without the base URL
   */
  public String shownPath() {
    return getCurrentPath();
  }

  /**
   * Waits for cart items to be visible.
   *
   * @return this page for method chaining
   */
  public CartPage waitForItems() {
    waitFor(CART_ITEM);
    return this;
  }
}
