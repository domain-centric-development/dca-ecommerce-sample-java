package dev.domaincentric.sample.ecommerce.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.ReducedMotion;
import dev.domaincentric.sample.ecommerce.e2e.pages.BuyerInfoPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.CartPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * How the browser animates moving from one page of the shop to the next.
 *
 * <p>Each page the browser opens records the transition it was revealed with: whether there was
 * one, and the animations its parts ran — the part named by its pseudo-element, e.g. {@code
 * ::view-transition-new(root)} for the page content. A part that is not animated runs none.
 */
@DisplayName("Page Transition E2E Tests")
class PageTransitionE2ETest extends BaseE2ETest {

  private static final String PRODUCT = "Domain-Driven Design";

  private static final String RECORD_TRANSITION =
      """
      window.pageTransition = null;
      addEventListener('pagereveal', (event) => {
        const transition = event.viewTransition;
        if (!transition) {
          window.pageTransition = { animated: false, animations: [] };
          return;
        }
        transition.ready.then(
          () => {
            window.pageTransition = {
              animated: true,
              animations: document.getAnimations()
                .filter((a) => (a.effect?.pseudoElement ?? '').startsWith('::view-transition'))
                .map((a) => ({
                  part: a.effect.pseudoElement,
                  name: a.animationName ?? '',
                  duration: Number(a.effect.getComputedTiming().duration) || 0,
                })),
            };
          },
          () => { window.pageTransition = { animated: false, animations: [] }; });
      });
      """;

  @BeforeEach
  void recordEveryPageTransition() {
    context.addInitScript(RECORD_TRANSITION);
  }

  @Test
  @DisplayName("The frame stays while the page changes")
  void theContentFadesWhileHeaderLogoAndFooterStayInPlace() {
    final ProductCatalogPage catalog = ProductCatalogPage.navigateTo(page);
    final String headerBefore = placeOf(page.locator("[data-test='site-header']"));
    final String logoBefore = placeOf(page.locator("[data-test='site-logo']"));

    catalog.viewProductNamed(PRODUCT);

    final Transition transition = transitionOf(page);
    assertTrue(transition.animated(), "the page change is animated");
    assertEquals(List.of("vt-fade-out"), transition.namesOf("::view-transition-old(root)"));
    assertEquals(List.of("vt-slide-up"), transition.namesOf("::view-transition-new(root)"));
    for (final String part : List.of("site-header", "site-logo", "site-footer")) {
      assertTrue(transition.has("::view-transition-group(" + part + ")"), part + " is its own");
    }
    for (final String part : List.of("site-header", "site-footer")) {
      assertFalse(transition.has("::view-transition-old(" + part + ")"), part + " not faded");
      assertFalse(transition.has("::view-transition-new(" + part + ")"), part + " not faded");
    }
    assertEquals(headerBefore, placeOf(page.locator("[data-test='site-header']")), "header");
    assertEquals(logoBefore, placeOf(page.locator("[data-test='site-logo']")), "logo");
  }

  @Test
  @DisplayName("The product image carries over")
  void theProductImageMovesFromTheCardToTheProductPage() {
    ProductCatalogPage.navigateTo(page);
    final Locator card = cardOf(page, PRODUCT);
    final String productId =
        card.locator("[data-test='view-product']").getAttribute("href").replace("/products/", "");
    final String onTheCard = placeOf(card.locator(".product-card__image"));

    card.locator("[data-test='view-product']").click();

    assertThat(page.locator("[data-test='product-detail-title']")).hasText(PRODUCT);
    final Transition transition = transitionOf(page);
    assertTrue(transition.animated(), "the page change is animated");
    assertTrue(
        transition.namesOf("::view-transition-group(product-img-" + productId + ")").stream()
            .anyMatch(name -> name.startsWith("-ua-view-transition-group-anim")),
        "the image moves from the card to the product page: " + transition);
    assertNotEquals(
        onTheCard,
        placeOf(page.locator(".product-detail__image")),
        "the card and the product page hold the image in different places");
  }

  @Test
  @DisplayName("The checkout progress stays anchored")
  void theProgressBarStaysWhileTheBuyerStepGivesWayToTheDeliveryStep() {
    ProductCatalogPage.navigateTo(page).viewProductNamed(PRODUCT).addToCart();
    final BuyerInfoPage buyer = CartPage.navigateTo(page).proceedToCheckout();
    buyer.fillBuyerInfo("guest@example.com", "Test", "Guest", "+1-555-0100");
    final String progressBefore = placeOf(page.locator("[data-test='checkout-progress']"));

    buyer.continueToDelivery();

    assertThat(page).hasURL(BASE_URL + "/checkout/delivery");
    final Transition transition = transitionOf(page);
    assertTrue(transition.animated(), "the step change is animated");
    assertEquals(List.of("vt-slide-up"), transition.namesOf("::view-transition-new(root)"));
    assertTrue(
        transition.has("::view-transition-group(checkout-progress)"),
        "the progress bar is its own part: " + transition);
    assertEquals(
        progressBefore, placeOf(page.locator("[data-test='checkout-progress']")), "progress bar");
  }

  @Test
  @DisplayName("Reduced motion means no animation")
  void theProductPageAppearsWithoutAnimationWhenTheVisitorPrefersReducedMotion() {
    context.close();
    context =
        browser.newContext(new Browser.NewContextOptions().setReducedMotion(ReducedMotion.REDUCE));
    context.addInitScript(RECORD_TRANSITION);
    page = context.newPage();
    ProductCatalogPage.navigateTo(page);

    cardOf(page, PRODUCT).locator("[data-test='view-product']").click();

    assertThat(page.locator("[data-test='product-detail-title']")).hasText(PRODUCT);
    final Transition transition = transitionOf(page);
    assertEquals(
        List.of(),
        transition.animations().stream().filter(Animation::moves).toList(),
        "nothing on the product page is animated");
  }

  private static Transition transitionOf(final Page page) {
    page.waitForFunction("() => window.pageTransition !== null");
    @SuppressWarnings("unchecked")
    final Map<String, Object> recorded =
        (Map<String, Object>) page.evaluate("() => window.pageTransition");
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> animations =
        (List<Map<String, Object>>) recorded.get("animations");
    return new Transition(
        Boolean.TRUE.equals(recorded.get("animated")),
        animations.stream()
            .map(
                a ->
                    new Animation(
                        (String) a.get("part"),
                        (String) a.get("name"),
                        ((Number) a.get("duration")).doubleValue()))
            .toList());
  }

  private static Locator cardOf(final Page page, final String name) {
    return page.locator("[data-test='product-card']")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator(
                        "[data-test='product-card-title']",
                        new Page.LocatorOptions()
                            .setHasText(Pattern.compile("^\\s*" + name + "\\s*$")))));
  }

  /** Where the element sits in the page, whatever the page is scrolled to. */
  private static String placeOf(final Locator locator) {
    assertThat(locator).isVisible();
    return (String)
        locator.evaluate(
            "e => { const r = e.getBoundingClientRect(); return (r.left + scrollX) + ',' + (r.top + scrollY); }");
  }

  private record Animation(String part, String name, double duration) {

    /** Anything that lasts a millisecond or more is motion the visitor sees. */
    boolean moves() {
      return duration >= 1;
    }
  }

  private record Transition(boolean animated, List<Animation> animations) {

    boolean has(final String part) {
      return animations.stream().anyMatch(a -> a.part().equals(part));
    }

    List<String> namesOf(final String part) {
      return animations.stream().filter(a -> a.part().equals(part)).map(Animation::name).toList();
    }
  }
}
