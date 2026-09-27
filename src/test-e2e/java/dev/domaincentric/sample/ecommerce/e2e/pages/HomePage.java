package dev.domaincentric.sample.ecommerce.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.BoundingBox;
import java.util.ArrayList;
import java.util.List;

/**
 * Page object for the home page at {@code /}.
 *
 * <p>Reads the welcome the hero section shows and the "Discover products" slider below it. A slider
 * card is <em>in view</em> when the whole card lies inside the slider as the browser lays it out
 * and inside the window's width — judged from the rendered boxes, never from class names or markup.
 */
public class HomePage extends BasePage {

  private static final String URL_PATTERN = "/";
  private static final String HERO = "hero";
  private static final String SLIDER = "product-slider";
  private static final String SLIDER_TITLE = "product-slider-title";
  private static final String CARD = "product-slider-card";
  private static final String CARD_IMAGE = "product-slider-card-image";
  private static final String CARD_NAME = "product-slider-card-name";
  private static final String CARD_PRICE = "product-slider-card-price";
  private static final String CARD_LINK = "product-slider-card-link";
  private static final String PREVIOUS = "product-slider-previous";
  private static final String NEXT = "product-slider-next";

  /** How long a paging step may take to settle (smooth scrolling) before a check gives up. */
  private static final double SETTLE_TIMEOUT_MS = 5_000;

  private static final String CARD_IN_VIEW =
      """
      ([slider, card, index]) => {
          const s = document.querySelector(`[data-test='${slider}']`);
          const c = s && s.querySelectorAll(`[data-test='${card}']`)[index];
          if (!c) return false;
          const sb = s.getBoundingClientRect();
          const cb = c.getBoundingClientRect();
          const left = Math.max(sb.left, 0) - 1;
          const right = Math.min(sb.right, document.documentElement.clientWidth) + 1;
          return cb.width > 0 && cb.left >= left && cb.right <= right;
      }""";

  /** The left and top edge of a card as the browser lays it out. */
  public record Position(double left, double top) {}

  /**
   * Creates a new HomePage and waits for its hero section.
   *
   * @param page the Playwright page instance
   */
  public HomePage(Page page) {
    super(page, URL_PATTERN);
    waitFor(HERO);
  }

  /**
   * Navigates to the home page and creates a page object.
   *
   * @param page the Playwright page instance
   * @return a new HomePage
   */
  public static HomePage navigateTo(Page page) {
    page.navigate(BASE_URL + URL_PATTERN);
    return new HomePage(page);
  }

  /**
   * The hero's heading as the visitor reads it.
   *
   * @return the heading text
   */
  public String heading() {
    return heroText("h1");
  }

  /**
   * The line under the heading.
   *
   * @return the subtitle text
   */
  public String subtitle() {
    return heroText(".hero__subtitle");
  }

  /**
   * The text below the subtitle.
   *
   * @return the description text
   */
  public String description() {
    return heroText(".hero__description");
  }

  /** Reloads the page and waits for its hero again. */
  public void reload() {
    page.reload();
    waitFor(HERO);
  }

  /**
   * Waits for the slider and scrolls it into the window, as a shopper would before looking at it.
   *
   * @return false when the page shows no slider
   */
  public boolean showSlider() {
    final Locator slider = byTest(SLIDER);
    try {
      slider.waitFor(new Locator.WaitForOptions().setTimeout(SETTLE_TIMEOUT_MS));
    } catch (PlaywrightException e) {
      return false;
    }
    slider.scrollIntoViewIfNeeded();
    return true;
  }

  /**
   * The slider's heading as the browser renders it.
   *
   * @return the heading text
   */
  public String sliderTitle() {
    return byTest(SLIDER_TITLE).innerText().strip();
  }

  /**
   * The {@code data-test} of the element that follows the hero on the page.
   *
   * @return its value, or null when nothing follows or it carries none
   */
  public String sectionAfterHero() {
    return (String)
        page.evaluate(
            "hero => document.querySelector(`[data-test='${hero}']`)?.nextElementSibling"
                + "?.getAttribute('data-test') ?? null",
            HERO);
  }

  /**
   * Whether the slider is laid out below the hero and above the given section.
   *
   * @param followingSection the {@code data-test} of the section that should follow
   * @return true when the slider stands between the two
   */
  public boolean sliderStandsBetweenHeroAnd(final String followingSection) {
    final BoundingBox hero = byTest(HERO).boundingBox();
    final BoundingBox slider = byTest(SLIDER).boundingBox();
    final BoundingBox following = byTest(followingSection).boundingBox();
    return hero != null
        && slider != null
        && following != null
        && slider.y >= hero.y + hero.height - 1
        && slider.y + slider.height <= following.y + 1;
  }

  /**
   * The number of cards the slider holds.
   *
   * @return the card count
   */
  public int cardCount() {
    return cards().count();
  }

  /**
   * The product names on the cards, in slider order.
   *
   * @return one name per card
   */
  public List<String> cardNames() {
    return page
        .locator("[data-test='" + SLIDER + "'] [data-test='" + CARD_NAME + "']")
        .allInnerTexts()
        .stream()
        .map(String::strip)
        .toList();
  }

  /**
   * The product name on one card.
   *
   * @param index the card's position in the slider, from 0
   * @return the name
   */
  public String cardName(final int index) {
    return cardPart(index, CARD_NAME).innerText().strip();
  }

  /**
   * The price on one card.
   *
   * @param index the card's position in the slider, from 0
   * @return the price text
   */
  public String cardPrice(final int index) {
    return cardPart(index, CARD_PRICE).innerText().strip();
  }

  /**
   * The image the card shows: the address the browser loaded.
   *
   * @param index the card's position in the slider, from 0
   * @return the address, empty when the card shows no picture
   */
  public String cardImageSource(final int index) {
    return (String)
        cardPart(index, CARD_IMAGE).evaluate("e => e.currentSrc || e.getAttribute('src') || ''");
  }

  /**
   * Whether the card's image has loaded and has a size.
   *
   * @param index the card's position in the slider, from 0
   * @return true when the picture is shown
   */
  public boolean cardImageIsLoaded(final int index) {
    return Boolean.TRUE.equals(
        cardPart(index, CARD_IMAGE)
            .evaluate(
                "async e => { if (!e.complete && e.decode) await e.decode().catch(() => {});"
                    + " return !!e.complete && e.naturalWidth > 0; }"));
  }

  /**
   * The path the card's link leads to.
   *
   * @param index the card's position in the slider, from 0
   * @return the link's target
   */
  public String cardLinkPath(final int index) {
    final String href = cardPart(index, CARD_LINK).getAttribute("href");
    return href == null ? "" : href;
  }

  /**
   * Follows the card's link.
   *
   * @param index the card's position in the slider, from 0
   * @return the product page it leads to
   */
  public ProductDetailPage followCard(final int index) {
    cardPart(index, CARD_LINK).click();
    return new ProductDetailPage(page);
  }

  /**
   * The left and top edge of every card, in slider order.
   *
   * @return one position per card
   */
  public List<Position> cardPositions() {
    final List<Position> positions = new ArrayList<>();
    for (int i = 0; i < cardCount(); i++) {
      final BoundingBox box = cards().nth(i).boundingBox();
      positions.add(
          box == null ? new Position(Double.NaN, Double.NaN) : new Position(box.x, box.y));
    }
    return positions;
  }

  /**
   * Whether the card is in view right now.
   *
   * @param index the card's position in the slider, from 0
   * @return true when the whole card is in view
   */
  public boolean isCardInView(final int index) {
    return Boolean.TRUE.equals(page.evaluate(CARD_IN_VIEW, List.of(SLIDER, CARD, index)));
  }

  /**
   * The indexes of the cards in view once paging has settled: waits until exactly the expected
   * cards are in view, then reports what is in view.
   *
   * @param expected the indexes expected in view
   * @return the indexes in view
   */
  public List<Integer> cardsInView(final Integer... expected) {
    try {
      page.waitForFunction(
          "args => { const inView = "
              + CARD_IN_VIEW
              + "; const n = document.querySelectorAll(`[data-test='${args[0]}']"
              + " [data-test='${args[1]}']`).length; const shown = [];"
              + " for (let i = 0; i < n; i++) if (inView([args[0], args[1], i])) shown.push(i);"
              + " return JSON.stringify(shown) === JSON.stringify(args[2]); }",
          List.of(SLIDER, CARD, List.of(expected)),
          new Page.WaitForFunctionOptions().setTimeout(SETTLE_TIMEOUT_MS));
    } catch (PlaywrightException e) {
      // Settled elsewhere: report what is in view now, so the assertion names it.
    }
    final List<Integer> shown = new ArrayList<>();
    for (int i = 0; i < cardCount(); i++) {
      if (isCardInView(i)) {
        shown.add(i);
      }
    }
    return shown;
  }

  /** Presses "Next". */
  public void pressNext() {
    byTest(NEXT).click();
  }

  /** Presses "Previous". */
  public void pressPrevious() {
    byTest(PREVIOUS).click();
  }

  /**
   * Whether "Next" is disabled once paging has settled.
   *
   * @return true when it is disabled
   */
  public boolean isNextDisabled() {
    return settlesTo(NEXT, true);
  }

  /**
   * Whether "Previous" is disabled once paging has settled.
   *
   * @return true when it is disabled
   */
  public boolean isPreviousDisabled() {
    return settlesTo(PREVIOUS, true);
  }

  /**
   * Whether "Next" is enabled once paging has settled.
   *
   * @return true when it is enabled
   */
  public boolean isNextEnabled() {
    return settlesTo(NEXT, false);
  }

  /**
   * Whether "Previous" is enabled once paging has settled.
   *
   * @return true when it is enabled
   */
  public boolean isPreviousEnabled() {
    return settlesTo(PREVIOUS, false);
  }

  /**
   * Presses Tab until the keyboard focus is on "Next".
   *
   * @return false when it never gets there
   */
  public boolean tabToNext() {
    for (int press = 0; press < 200; press++) {
      page.keyboard().press("Tab");
      final Object focused =
          page.evaluate("() => document.activeElement?.getAttribute('data-test') ?? null");
      if (NEXT.equals(focused)) {
        return true;
      }
    }
    return false;
  }

  /** Presses Enter on whatever has the keyboard focus. */
  public void pressEnter() {
    page.keyboard().press("Enter");
  }

  private Locator cards() {
    return page.locator("[data-test='" + SLIDER + "'] [data-test='" + CARD + "']");
  }

  private Locator cardPart(final int index, final String dataTest) {
    return cards().nth(index).locator("[data-test='" + dataTest + "']");
  }

  private Locator byTest(final String dataTest) {
    return page.locator("[data-test='" + dataTest + "']");
  }

  private boolean settlesTo(final String dataTest, final boolean disabled) {
    try {
      page.waitForFunction(
          "([dt, disabled]) => document.querySelector(`[data-test='${dt}']`)?.disabled === disabled",
          List.of(dataTest, disabled),
          new Page.WaitForFunctionOptions().setTimeout(SETTLE_TIMEOUT_MS));
      return true;
    } catch (PlaywrightException e) {
      return false;
    }
  }

  // The text as written, not as CSS transforms it: the subtitle is styled in capitals.
  private String heroText(String selector) {
    return page.locator("[data-test='" + HERO + "'] " + selector).textContent().strip();
  }
}
