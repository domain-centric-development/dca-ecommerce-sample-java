package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import java.time.Duration;
import java.util.List;
import java.util.Map;

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
   * Records, when a page is revealed, whether a transition between pages ran and which of the
   * transition's parts were animated at that moment; {@link #reveal()} reads it.
   */
  private static final String RECORD_THE_REVEAL =
      """
      window.addEventListener('pagereveal', (event) => {
        const transition = event.viewTransition;
        if (!transition) {
          window.revealedTransition = { ran: false, animated: [] };
          return;
        }
        transition.ready.then(
          () => {
            window.revealedTransition = {
              ran: true,
              animated: document.getAnimations()
                .map((animation) => animation.effect && animation.effect.pseudoElement)
                .filter((part) => part && part.startsWith('::view-transition'))
            };
          },
          () => { window.revealedTransition = { ran: false, animated: [] }; });
      });
      """;

  /**
   * How the page was revealed.
   *
   * @param ran whether a transition between the pages ran
   * @param animatedParts the parts of the transition that were animated
   */
  public record Reveal(boolean ran, List<String> animatedParts) {}

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
   * The stock badge beside the product's heading, as the page words it (the style sets it in
   * capitals).
   *
   * @return the badge text, trimmed
   */
  public String stockBadge() {
    return page.locator("[data-test='" + PRODUCT_DETAIL + "'] .product-detail__header .badge")
        .textContent()
        .strip();
  }

  /**
   * The value the page shows under a meta label, such as "Availability".
   *
   * @param label the label as the page shows it
   * @return the value text, trimmed
   */
  public String metaValue(String label) {
    return page.locator(".product-detail__meta-item")
        .filter(
            new com.microsoft.playwright.Locator.FilterOptions()
                .setHas(
                    page.locator(
                        ".product-detail__meta-label",
                        new Page.LocatorOptions().setHasText(label))))
        .locator(".product-detail__meta-value")
        .textContent()
        .strip();
  }

  /**
   * The value under a meta label once it reads the expected text — the page reloaded until it does
   * or the time is up, for a value the shop changes shortly after something happened elsewhere.
   *
   * @param label the label as the page shows it
   * @param expected the value waited for
   * @param within how long to wait for it
   * @return the value last read, trimmed
   */
  public String metaValueOnceItReads(String label, String expected, Duration within) {
    final long deadline = System.nanoTime() + within.toNanos();
    String value = metaValue(label);
    while (!expected.equals(value) && System.nanoTime() < deadline) {
      page.reload();
      value = metaValue(label);
    }
    return value;
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
   * Makes every page the context opens record how it was revealed, for {@link #reveal()}. Called
   * before the first page of the context is opened.
   *
   * @param context the browser context of the test
   */
  public static void recordRevealsIn(BrowserContext context) {
    context.addInitScript(RECORD_THE_REVEAL);
  }

  /**
   * How this page was revealed, once it has recorded it.
   *
   * @return whether a transition ran and which of its parts animated
   */
  @SuppressWarnings("unchecked")
  public Reveal reveal() {
    page.waitForFunction("() => window.revealedTransition !== undefined");
    final Map<String, Object> recorded =
        (Map<String, Object>) page.evaluate("() => window.revealedTransition");
    return new Reveal(
        Boolean.TRUE.equals(recorded.get("ran")),
        List.copyOf((List<String>) recorded.get("animated")));
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
