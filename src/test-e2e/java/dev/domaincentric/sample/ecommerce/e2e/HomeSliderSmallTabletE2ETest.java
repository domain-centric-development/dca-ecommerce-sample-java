package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The "Discover products" slider on a small tablet (size m, 768 px, the upper edge of 481 to 768
 * px): two cards in view side by side.
 */
@DisplayName("Home slider on a small tablet E2E Tests")
class HomeSliderSmallTabletE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setViewportSize(768, 1024);
  }

  @Test
  @DisplayName("On a small tablet the slider shows two of its cards side by side")
  void onASmallTabletTheFirstTwoCardsStandSideBySide() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");

    assertEquals(8, home.cardCount());
    assertEquals(List.of(0, 1), home.cardsInView(0, 1));
    HomeSliderE2ETest.assertSideBySide(home.cardPositions());
  }
}
