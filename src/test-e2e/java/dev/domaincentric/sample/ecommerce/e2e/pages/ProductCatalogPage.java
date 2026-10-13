package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the product catalog page.
 *
 * <p>Provides methods to interact with the product listing, including viewing product details.
 */
public class ProductCatalogPage extends BasePage {

  private static final String URL_PATTERN = "/products";
  private static final String PRODUCT_CARD = "product-card";
  private static final String VIEW_DETAILS_LINK = "view-product";
  private static final String PRODUCT_TITLE = "product-card-title";
  private static final String ERROR_MESSAGE = "error-message";
  // The card's price carries no data-test; adopting the page changes no template.
  private static final String CARD_PRICE = ".product-card__price";

  /**
   * Creates a new ProductCatalogPage and waits for it to load.
   *
   * @param page the Playwright page instance
   */
  public ProductCatalogPage(Page page) {
    super(page, URL_PATTERN);
    waitFor(PRODUCT_CARD);
  }

  /**
   * Navigates to the product catalog and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new ProductCatalogPage
   */
  public static ProductCatalogPage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new ProductCatalogPage(page);
  }

  /**
   * Creates the catalog page of a shop the test started itself and waits for it to load.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   */
  public ProductCatalogPage(Page page, String shopUrl) {
    super(page, shopUrl, URL_PATTERN);
    waitFor(PRODUCT_CARD);
  }

  /**
   * Navigates to the catalog of a shop the test started itself.
   *
   * @param page the Playwright page instance
   * @param shopUrl the base URL of that shop
   * @return a new ProductCatalogPage
   */
  public static ProductCatalogPage navigateTo(Page page, String shopUrl) {
    page.navigate(shopUrl + URL_PATTERN);
    return new ProductCatalogPage(page, shopUrl);
  }

  /**
   * Whether the page shows an error message above its content.
   *
   * @return true when the error message is shown
   */
  public boolean showsErrorMessage() {
    return exists(ERROR_MESSAGE);
  }

  /**
   * Clicks "View Details" on the first product in the catalog.
   *
   * @return the ProductDetailPage for the selected product
   */
  public ProductDetailPage viewFirstProduct() {
    clickFirst(VIEW_DETAILS_LINK);
    return new ProductDetailPage(page);
  }

  /**
   * Clicks "View Details" on the product at the given position (0-based) in the catalog.
   *
   * @param index the position of the product card
   * @return the ProductDetailPage for the selected product
   */
  public ProductDetailPage viewProduct(int index) {
    page.locator("[data-test='" + VIEW_DETAILS_LINK + "']").nth(index).click();
    return new ProductDetailPage(page);
  }

  /**
   * Clicks "View Details" on the card of the named product.
   *
   * @param name the product name as its card shows it
   * @return the ProductDetailPage for that product
   */
  public ProductDetailPage viewProductNamed(String name) {
    cardNamed(name).locator("[data-test='" + VIEW_DETAILS_LINK + "']").click();
    return new ProductDetailPage(page);
  }

  /**
   * The price on the card of the named product.
   *
   * @param name the product name as its card shows it
   * @return the price text, trimmed
   */
  public String cardPriceOf(String name) {
    return cardNamed(name).locator(CARD_PRICE).innerText().strip();
  }

  private com.microsoft.playwright.Locator cardNamed(String name) {
    return page.locator("[data-test='" + PRODUCT_CARD + "']")
        .filter(
            new com.microsoft.playwright.Locator.FilterOptions()
                .setHas(
                    page.locator(
                        "[data-test='" + PRODUCT_TITLE + "']",
                        new Page.LocatorOptions().setHasText(name))))
        .first();
  }

  /**
   * Checks if product cards are displayed.
   *
   * @return true if at least one product card exists
   */
  public boolean hasProducts() {
    return exists(PRODUCT_CARD);
  }

  /**
   * The product names as the catalog shows them, in page order.
   *
   * @return one entry per product card
   */
  public java.util.List<String> productTitles() {
    return page.locator("[data-test='" + PRODUCT_TITLE + "']").allTextContents().stream()
        .map(String::trim)
        .toList();
  }
}
