package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.ReducedMotion;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A visitor whose device asks for less motion changes pages without animation.
 *
 * <p>Each page records, when it is revealed, whether a transition between pages ran and which of
 * the transition's parts were animated at that moment.
 */
@DisplayName("Reduced Motion E2E Tests")
class ReducedMotionE2ETest extends BaseE2ETest {

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

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setReducedMotion(ReducedMotion.REDUCE);
  }

  @Test
  @DisplayName("Reduced motion means no animation")
  void theProductPageAppearsWithoutAnimationWhenTheVisitorPrefersReducedMotion() {
    context.addInitScript(RECORD_THE_REVEAL);
    ProductCatalogPage catalogue = ProductCatalogPage.navigateTo(page);

    ProductDetailPage product = catalogue.viewProductNamed("Domain-Driven Design");

    assertEquals("Domain-Driven Design", product.title());
    page.waitForFunction("() => window.revealedTransition !== undefined");
    @SuppressWarnings("unchecked")
    Map<String, Object> reveal =
        (Map<String, Object>) page.evaluate("() => window.revealedTransition");
    assertEquals(Boolean.TRUE, reveal.get("ran"), "a transition between the pages ran");
    assertEquals(List.of(), reveal.get("animated"), "parts of the transition that animated");
  }
}
