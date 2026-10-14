package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.ReducedMotion;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A visitor whose device asks for less motion changes pages without animation.
 *
 * <p>Each page records, when it is revealed, whether a transition between pages ran and which of
 * the transition's parts were animated at that moment (see {@link ProductDetailPage#reveal()}).
 */
@DisplayName("Reduced Motion E2E Tests")
class ReducedMotionE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setReducedMotion(ReducedMotion.REDUCE);
  }

  @Test
  @DisplayName("Reduced motion means no animation")
  void theProductPageAppearsWithoutAnimationWhenTheVisitorPrefersReducedMotion() {
    ProductDetailPage.recordRevealsIn(context);
    ProductCatalogPage catalogue = ProductCatalogPage.navigateTo(page);

    ProductDetailPage product = catalogue.viewProductNamed("Domain-Driven Design");

    assertEquals("Domain-Driven Design", product.title());
    ProductDetailPage.Reveal reveal = product.reveal();
    assertTrue(reveal.ran(), "a transition between the pages ran");
    assertEquals(List.of(), reveal.animatedParts(), "parts of the transition that animated");
  }
}
