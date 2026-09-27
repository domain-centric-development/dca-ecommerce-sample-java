package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The "Discover products" slider on a phone (393 px, as the shop's other phone checks): one card in
 * view, paged by "Previous" and "Next" — by mouse or keyboard — and still unless the shopper pages
 * it.
 */
@DisplayName("Home slider on a phone E2E Tests")
class HomeSliderPhoneE2ETest extends BaseE2ETest {

  @Override
  protected Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setViewportSize(393, 852);
  }

  @Test
  @DisplayName("On a phone the slider shows one card at a time")
  void onAPhoneOnlyTheFirstCardIsInView() {
    HomePage home = openSlider();

    assertEquals(List.of(0), home.cardsInView(0));
    assertTrue(home.isPreviousDisabled(), "\"Previous\" is disabled at the first card");
  }

  @Test
  @DisplayName("On a phone Next brings the following slider card into view")
  void onAPhoneNextBringsTheSecondCardIntoView() {
    HomePage home = openSlider();
    assertEquals(List.of(0), home.cardsInView(0));

    home.pressNext();

    assertEquals(List.of(1), home.cardsInView(1));
  }

  @Test
  @DisplayName("On a phone Previous brings the preceding slider card back into view")
  void onAPhonePreviousBringsTheFirstCardBackIntoView() {
    HomePage home = openSlider();
    home.pressNext();
    assertEquals(List.of(1), home.cardsInView(1));

    home.pressPrevious();

    assertEquals(List.of(0), home.cardsInView(0));
  }

  @Test
  @DisplayName("On a phone the slider's Next button works from the keyboard")
  void onAPhoneNextWorksFromTheKeyboard() {
    HomePage home = openSlider();
    assertEquals(List.of(0), home.cardsInView(0));
    assertTrue(home.tabToNext(), "the Tab key reaches \"Next\"");

    home.pressEnter();

    assertEquals(List.of(1), home.cardsInView(1));
  }

  @Test
  @DisplayName("On a phone the slider stops at its last card")
  void onAPhoneTheSliderStopsAtItsLastCard() {
    HomePage home = openSlider();

    for (int press = 1; press <= 7; press++) {
      assertTrue(home.isNextEnabled(), "\"Next\" can be pressed a " + press + ". time");
      home.pressNext();
      assertEquals(List.of(press), home.cardsInView(press));
    }

    assertEquals(List.of(7), home.cardsInView(7));
    assertTrue(home.isNextDisabled(), "\"Next\" is disabled at the last card");
  }

  @Test
  @DisplayName("The homepage slider does not move by itself")
  void theSliderDoesNotMoveByItself() {
    page.clock().install();
    HomePage home = openSlider();
    assertTrue(home.isCardInView(0), "the first card is in view");

    // Ten seconds of nobody touching the slider, on the page's own clock: every timer the page
    // set fires as it would in that time.
    page.clock().runFor(10_000);

    assertTrue(home.isCardInView(0), "the first card is still in view");
    assertFalse(home.isCardInView(1), "the slider moved on to the second card by itself");
  }

  private HomePage openSlider() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    return home;
  }
}
