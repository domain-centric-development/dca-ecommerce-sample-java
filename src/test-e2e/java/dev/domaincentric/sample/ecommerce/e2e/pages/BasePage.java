package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Page;

/**
 * Base class for all page objects, providing common methods for page interactions.
 *
 * <p>Encapsulates Playwright interactions and provides a consistent API for:
 *
 * <ul>
 *   <li>Element interactions via data-test attributes
 *   <li>Form field operations
 *   <li>Navigation and URL validation
 * </ul>
 */
public abstract class BasePage {

  protected static final String BASE_URL =
      System.getProperty("e2e.baseUrl", "http://localhost:8080");
  protected final Page page;

  /**
   * Creates a new page object and validates the current URL matches the expected pattern.
   *
   * @param page the Playwright page instance
   * @param expectedUrlPattern glob pattern for the expected URL (e.g., "/products/*")
   */
  protected BasePage(Page page, String expectedUrlPattern) {
    this.page = page;
    page.waitForURL(BASE_URL + expectedUrlPattern);
  }

  /**
   * Creates a new page object without URL validation. Use this when the page might not be loaded
   * yet.
   *
   * @param page the Playwright page instance
   */
  protected BasePage(Page page) {
    this.page = page;
  }

  /**
   * Clicks an element by its data-test attribute value.
   *
   * @param dataTest the value of the data-test attribute
   */
  protected void click(String dataTest) {
    page.locator("[data-test='" + dataTest + "']").click();
  }

  /**
   * Clicks the first matching element by its data-test attribute value.
   *
   * @param dataTest the value of the data-test attribute
   */
  protected void clickFirst(String dataTest) {
    page.locator("[data-test='" + dataTest + "']").first().click();
  }

  /**
   * Fills a form field by its name attribute.
   *
   * @param name the name attribute of the input
   * @param value the value to fill
   */
  protected void fill(String name, String value) {
    page.locator("input[name='" + name + "']").fill(value);
  }

  /**
   * Waits for at least one element with the given data-test attribute to be visible.
   *
   * @param dataTest the value of the data-test attribute
   */
  protected void waitFor(String dataTest) {
    page.locator("[data-test='" + dataTest + "']").first().waitFor();
  }

  /**
   * Checks if an element with the given data-test attribute exists.
   *
   * @param dataTest the value of the data-test attribute
   * @return true if the element exists
   */
  protected boolean exists(String dataTest) {
    return page.locator("[data-test='" + dataTest + "']").count() > 0;
  }

  /**
   * Selects (checks) the first radio button with the given data-test attribute.
   *
   * @param dataTest the value of the data-test attribute
   */
  protected void selectFirstRadio(String dataTest) {
    page.locator("[data-test='" + dataTest + "']").first().check();
  }

  /**
   * Navigates to a path relative to the base URL.
   *
   * @param path the path to navigate to (e.g., "/cart")
   */
  /**
   * Checks that nothing on the page reaches past the viewport, i.e. the document cannot scroll
   * sideways.
   *
   * @return true if the page fits the viewport
   */
  public boolean fitsViewport() {
    return (Boolean)
        page.evaluate(
            "() => document.documentElement.scrollWidth <= document.documentElement.clientWidth");
  }

  protected void navigateTo(String path) {
    page.navigate(BASE_URL + path);
  }

  /**
   * Gets the current page URL path (without base URL).
   *
   * @return the current path
   */
  protected String getCurrentPath() {
    return page.url().replace(BASE_URL, "");
  }

  /**
   * Checks if the page contains the specified text.
   *
   * @param text the text to search for
   * @return true if the text is found
   */
  protected boolean pageContains(String text) {
    return page.locator("body").textContent().contains(text);
  }

  /**
   * Waits for a specified duration.
   *
   * @param milliseconds the duration to wait
   */
  protected void waitForTimeout(long milliseconds) {
    page.waitForTimeout(milliseconds);
  }
}
