package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the shopping cart page.
 *
 * <p>Provides methods to view cart contents and proceed to checkout.
 */
public class CartPage extends BasePage {

  private static final String URL_PATTERN = "/cart**";
  private static final String CART_ITEM = "cart-item";
  private static final String CHECKOUT_BUTTON = "cart-checkout-button";

  /**
   * Creates a new CartPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public CartPage(Page page) {
    super(page, URL_PATTERN);
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
   * Waits for cart items to be displayed and proceeds to checkout.
   *
   * @return the BuyerInfoPage to enter buyer information
   */
  public BuyerInfoPage proceedToCheckout() {
    waitFor(CART_ITEM);
    click(CHECKOUT_BUTTON);
    return new BuyerInfoPage(page);
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
   * Waits for cart items to be visible.
   *
   * @return this page for method chaining
   */
  public CartPage waitForItems() {
    waitFor(CART_ITEM);
    return this;
  }
}
