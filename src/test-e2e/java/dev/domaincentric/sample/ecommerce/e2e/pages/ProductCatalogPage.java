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
   * Checks if product cards are displayed.
   *
   * @return true if at least one product card exists
   */
  public boolean hasProducts() {
    return exists(PRODUCT_CARD);
  }
}
