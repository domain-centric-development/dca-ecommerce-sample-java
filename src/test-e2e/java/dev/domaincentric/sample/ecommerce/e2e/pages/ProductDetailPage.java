package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the product detail page.
 *
 * <p>Provides methods to view product details and add the product to the cart.
 */
public class ProductDetailPage extends BasePage {

  private static final String URL_PATTERN = "/products/*";
  private static final String PRODUCT_DETAIL = "product-detail";
  private static final String ADD_TO_CART_BUTTON = "product-add-to-cart-button";
  private static final String BACK_LINK = "product-back-link";
  private static final String TITLE = "product-detail-title";
  private static final String PRICE = "product-detail-price";

  /**
   * Creates a new ProductDetailPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public ProductDetailPage(Page page) {
    super(page, URL_PATTERN);
    waitFor(PRODUCT_DETAIL);
  }

  /**
   * Adds the current product to the shopping cart.
   *
   * @return the CartPage after adding the product
   */
  public CartPage addToCart() {
    click(ADD_TO_CART_BUTTON);
    page.waitForURL(BASE_URL + "/cart**");
    return new CartPage(page);
  }

  /**
   * Adds the current product to the cart and stays on the same page. Useful when adding multiple
   * products.
   *
   * @return this page for method chaining
   */
  public ProductDetailPage addToCartAndStay() {
    click(ADD_TO_CART_BUTTON);
    page.waitForURL(BASE_URL + "/cart**");
    return this;
  }

  /**
   * Navigates back to the product catalog.
   *
   * @return the ProductCatalogPage
   */
  public ProductCatalogPage backToCatalog() {
    click(BACK_LINK);
    return new ProductCatalogPage(page);
  }

  /**
   * The heading of the product page.
   *
   * @return the heading text, trimmed
   */
  public String heading() {
    return page.locator("[data-test='" + PRODUCT_DETAIL + "'] h1").textContent().trim();
  }

  /**
   * The product's name as the page's title shows it.
   *
   * @return the title text, trimmed
   */
  public String title() {
    return page.locator("[data-test='" + TITLE + "']").innerText().strip();
  }

  /**
   * The price the page shows.
   *
   * @return the price text, trimmed
   */
  public String price() {
    return page.locator("[data-test='" + PRICE + "']").innerText().strip();
  }

  /**
   * The address of the product image the page shows.
   *
   * @return the address the browser loaded, empty when the page shows no picture
   */
  public String imageSource() {
    return (String)
        page.locator("[data-test='" + PRODUCT_DETAIL + "'] img")
            .first()
            .evaluate("e => e.currentSrc || e.getAttribute('src') || ''");
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
   * Checks if the product detail section is displayed.
   *
   * @return true if product detail is visible
   */
  public boolean isDisplayed() {
    return exists(PRODUCT_DETAIL);
  }
}
