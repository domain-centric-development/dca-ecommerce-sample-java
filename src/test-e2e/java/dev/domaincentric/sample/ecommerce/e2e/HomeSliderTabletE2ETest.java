package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The "Discover products" slider on a large tablet (size l, 1024 px, inside 769 to 1180 px): four
 * cards in view, as on the desktop.
 */
@DisplayName("Home slider on a large tablet E2E Tests")
class HomeSliderTabletE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setViewportSize(1024, 768);
  }

  @Test
  @DisplayName("On a large tablet the slider shows four of its cards side by side")
  void onALargeTabletTheFirstFourCardsStandSideBySide() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");

    assertEquals(8, home.cardCount());
    assertEquals(List.of(0, 1, 2, 3), home.cardsInView(0, 1, 2, 3));
    HomeSliderE2ETest.assertSideBySide(home.cardPositions());
  }
}
