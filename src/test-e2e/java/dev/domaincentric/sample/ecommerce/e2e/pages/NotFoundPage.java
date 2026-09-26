package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Page object for the not-found page the shop shows for an address it has no page for.
 *
 * <p>Reads the code, heading and message the page shows. Only its two links carry a {@code
 * data-test} attribute, so the texts are read by the page's own classes.
 */
public class NotFoundPage extends BasePage {

  private static final String BROWSE_LINK = "error-browse-link";

  /**
   * Creates a new NotFoundPage and waits for its way on to the catalogue.
   *
   * @param page the Playwright page instance
   */
  public NotFoundPage(Page page) {
    super(page);
    waitFor(BROWSE_LINK);
  }

  /**
   * Opens the product page of a product id and expects the not-found page.
   *
   * @param page the Playwright page instance
   * @param productId an id no product has
   * @return the NotFoundPage shown for it
   */
  public static NotFoundPage openProduct(Page page, String productId) {
    page.navigate(BASE_URL + "/products/" + productId);
    return new NotFoundPage(page);
  }

  /**
   * The status code the page shows.
   *
   * @return the code text
   */
  public String code() {
    return text(".error-page__code");
  }

  /**
   * The page's heading.
   *
   * @return the heading text
   */
  public String heading() {
    return text(".error-page__title");
  }

  /**
   * The message under the heading, its line breaks read as single spaces.
   *
   * @return the message text
   */
  public String message() {
    return text(".error-page__message").replaceAll("\\s+", " ");
  }

  private String text(String selector) {
    return page.locator(".error-page " + selector).textContent().strip();
  }
}
