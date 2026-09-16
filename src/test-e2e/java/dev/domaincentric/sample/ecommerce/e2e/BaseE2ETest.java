package dev.domaincentric.sample.ecommerce.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;

/**
 * Base class for E2E tests using Playwright.
 *
 * <p>Provides common setup for browser automation including:
 *
 * <ul>
 *   <li>Playwright and browser initialization
 *   <li>Browser context and page management per test
 *   <li>Configuration via system properties
 *   <li>Common navigation and assertion helpers
 * </ul>
 *
 * <p>Configuration properties:
 *
 * <ul>
 *   <li>{@code e2e.baseUrl} - Base URL for the application (default: http://localhost:8080)
 *   <li>{@code e2e.browser} - Browser to use: chromium, firefox, webkit (default: chromium)
 *   <li>{@code e2e.headless} - Run in headless mode (default: true)
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * ./gradlew test-e2e -De2e.baseUrl=http://localhost:8080 -De2e.headless=false
 * }</pre>
 */
public abstract class BaseE2ETest {

  protected static final String BASE_URL =
      System.getProperty("e2e.baseUrl", "http://localhost:8080");
  protected static final String BROWSER_TYPE = System.getProperty("e2e.browser", "chromium");
  protected static final boolean HEADLESS =
      Boolean.parseBoolean(System.getProperty("e2e.headless", "true"));

  protected static Playwright playwright;
  protected static Browser browser;

  protected BrowserContext context;
  protected Page page;

  @RegisterExtension
  final TestWatcher screenshotOnFailure =
      new TestWatcher() {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
          if (page != null && !page.isClosed()) {
            try {
              String testName = context.getDisplayName().replaceAll("[^a-zA-Z0-9_-]", "_");
              page.screenshot(
                  new Page.ScreenshotOptions()
                      .setPath(Paths.get("build/reports/test-e2e/screenshots/" + testName + ".png"))
                      .setFullPage(true));
            } catch (Exception e) {
              // Browser context may already be closed by @AfterEach
            }
          }
        }
      };

  @BeforeAll
  static void launchBrowser() {
    playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setHeadless(HEADLESS);

    browser =
        switch (BROWSER_TYPE.toLowerCase()) {
          case "firefox" -> playwright.firefox().launch(options);
          case "webkit" -> playwright.webkit().launch(options);
          default -> playwright.chromium().launch(options);
        };
  }

  @AfterAll
  static void closeBrowser() {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  @BeforeEach
  void createContextAndPage() {
    context = browser.newContext(contextOptions());
    page = context.newPage();
  }

  /**
   * Options for the browser context of each test. A suite that needs a phone viewport overrides
   * this; the default is Playwright's desktop viewport.
   *
   * @return the context options
   */
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions();
  }

  @AfterEach
  void closeContext() {
    if (context != null) {
      context.close();
    }
  }

  /**
   * Navigates to a path relative to the base URL.
   *
   * @param path the path to navigate to (e.g., "/cart")
   */
  protected void navigateTo(String path) {
    page.navigate(BASE_URL + path);
  }

  /**
   * Fills a form field by its name attribute.
   *
   * @param name the name attribute of the input
   * @param value the value to fill
   */
  protected void fillByName(String name, String value) {
    page.locator("input[name=\"" + name + "\"]").fill(value);
  }

  /**
   * Clicks a button by its text content.
   *
   * @param text the button text
   * @deprecated Use {@link #clickTestElement(String)} with data-test attributes instead (ADR-017)
   */
  @Deprecated
  protected void clickButton(String text) {
    page.locator("button:has-text(\"" + text + "\")").click();
  }

  /**
   * Clicks a link by its text content.
   *
   * @param text the link text
   * @deprecated Use {@link #clickTestElement(String)} with data-test attributes instead (ADR-017)
   */
  @Deprecated
  protected void clickLink(String text) {
    page.locator("a:has-text(\"" + text + "\")").click();
  }

  /**
   * Clicks an element by its data-test attribute value.
   *
   * @param dataTestValue the value of the data-test attribute
   */
  protected void clickTestElement(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "']").click();
  }

  /**
   * Clicks the first matching element by its data-test attribute value.
   *
   * @param dataTestValue the value of the data-test attribute
   */
  protected void clickFirstTestElement(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "']").first().click();
  }

  /**
   * Waits for a specific URL pattern.
   *
   * @param urlPattern the URL pattern to wait for (can be a glob pattern)
   */
  protected void waitForUrl(String urlPattern) {
    page.waitForURL(BASE_URL + urlPattern);
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
   * Gets the text content of an element by CSS selector.
   *
   * @param selector the CSS selector
   * @return the text content
   */
  protected String getText(String selector) {
    return page.locator(selector).textContent();
  }

  /**
   * Checks if an element exists on the page.
   *
   * @param selector the CSS selector
   * @return true if the element exists
   */
  protected boolean elementExists(String selector) {
    return page.locator(selector).count() > 0;
  }

  /**
   * Checks if an element with the given data-test attribute exists on the page.
   *
   * @param dataTestValue the value of the data-test attribute
   * @return true if the element exists
   */
  protected boolean testElementExists(String dataTestValue) {
    return page.locator("[data-test='" + dataTestValue + "']").count() > 0;
  }

  /**
   * Waits for at least one element matching the selector to be visible.
   *
   * @param selector the CSS selector
   */
  protected void waitForElement(String selector) {
    page.locator(selector).first().waitFor();
  }

  /**
   * Waits for at least one element with the given data-test attribute to be visible.
   *
   * @param dataTestValue the value of the data-test attribute
   */
  protected void waitForTestElement(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "']").first().waitFor();
  }

  /**
   * Submits a form by clicking the submit button within it.
   *
   * @param formSelector the CSS selector for the form
   * @deprecated Use {@link #submitTestForm(String)} with data-test attributes instead (ADR-017)
   */
  @Deprecated
  protected void submitForm(String formSelector) {
    page.locator(formSelector + " button[type=\"submit\"]").click();
  }

  /**
   * Submits a form by clicking the submit button within a form identified by data-test attribute.
   *
   * @param dataTestValue the value of the data-test attribute on the form
   */
  protected void submitTestForm(String dataTestValue) {
    page.locator("[data-test='" + dataTestValue + "'] button[type=\"submit\"]").click();
  }

  /**
   * Selects an option from a dropdown by its value.
   *
   * @param name the name attribute of the select element
   * @param value the value to select
   */
  protected void selectByName(String name, String value) {
    page.locator("select[name=\"" + name + "\"]").selectOption(value);
  }

  /**
   * Checks a checkbox by its name attribute.
   *
   * @param name the name attribute of the checkbox
   */
  protected void checkByName(String name) {
    page.locator("input[type=\"checkbox\"][name=\"" + name + "\"]").check();
  }

  /**
   * Takes a screenshot for debugging purposes.
   *
   * @param name the screenshot file name (without extension)
   */
  protected void takeScreenshot(String name) {
    page.screenshot(
        new Page.ScreenshotOptions()
            .setPath(
                java.nio.file.Paths.get("build/reports/test-e2e/screenshots/" + name + ".png")));
  }
}
