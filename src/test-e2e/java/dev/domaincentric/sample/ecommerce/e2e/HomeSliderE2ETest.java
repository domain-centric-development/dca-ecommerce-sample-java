package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage.Position;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductDetailPage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The "Discover products" slider on the homepage, in the suite's default browser window (the
 * desktop): where it stands, what it holds, what a card shows and where it leads.
 *
 * <p>Same scenarios and page objects as the .NET sample's {@code HomeSliderE2eTest}.
 */
@DisplayName("Home slider E2E Tests")
class HomeSliderE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("The homepage shows a \"Discover products\" slider directly below the hero")
  void showsTheDiscoverProductsSliderDirectlyBelowTheHero() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");

    assertEquals("Discover products", home.sliderTitle());
    assertEquals("product-slider", home.sectionAfterHero());
    assertTrue(
        home.sliderStandsBetweenHeroAnd("features"),
        "the slider stands below the hero and before \"Why Shop With Us\"");
  }

  @Test
  @DisplayName("The homepage slider holds eight different products of the sample catalog")
  void holdsEightDifferentProductsOfTheSampleCatalog() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    List<String> names = home.cardNames();

    assertEquals(8, home.cardCount());
    assertEquals(8, Set.copyOf(names).size(), "the cards show different products: " + names);
    List<String> catalog = ProductCatalogPage.navigateTo(page).productTitles();
    assertTrue(catalog.containsAll(names), names + " are products of the sample catalog");
  }

  @Test
  @DisplayName("The homepage slider draws its products anew on every request")
  void drawsItsProductsAnewOnEveryRequest() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    Set<String> noted = new HashSet<>(home.cardNames());
    assertEquals(8, noted.size());

    int differentSelections = 0;
    for (int reload = 0; reload < 10; reload++) {
      home.reload();
      assertTrue(home.showSlider(), "the homepage shows the product slider");
      if (!noted.equals(new HashSet<>(home.cardNames()))) {
        differentSelections++;
      }
    }

    assertTrue(differentSelections >= 1, "ten reloads all showed the same eight products");
  }

  @Test
  @DisplayName("A slider card shows the product's image, name and price")
  void aCardShowsTheProductsImageNameAndPrice() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    String name = home.cardName(0);
    String price = home.cardPrice(0);
    String image = home.cardImageSource(0);
    assertTrue(home.cardImageIsLoaded(0), "the card shows the product's image");

    navigateTo(home.cardLinkPath(0));
    ProductDetailPage product = new ProductDetailPage(page);

    assertEquals(product.title(), name);
    assertEquals(product.price(), price);
    assertEquals(product.imageSource(), image);
  }

  @Test
  @DisplayName("A slider card leads to its product page")
  void aCardLeadsToItsProductPage() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    String name = home.cardName(1);
    String link = home.cardLinkPath(1);
    assertTrue(link.matches("^/products/[0-9a-f-]{36}$"), "the card links a product page: " + link);

    ProductDetailPage product = home.followCard(1);

    assertEquals(link, product.shownPath());
    assertEquals(name, product.title());
  }

  @Test
  @DisplayName("On the desktop the slider shows four of its cards side by side")
  void onTheDesktopTheFirstFourCardsStandSideBySide() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");

    assertEquals(8, home.cardCount());
    assertEquals(List.of(0, 1, 2, 3), home.cardsInView(0, 1, 2, 3));
    assertSideBySide(home.cardPositions());
    assertTrue(home.isPreviousDisabled(), "\"Previous\" is disabled");
    assertTrue(home.isNextEnabled(), "\"Next\" is enabled");
  }

  @Test
  @DisplayName("On the desktop Next moves the slider on by one card")
  void onTheDesktopNextMovesTheSliderOnByOneCard() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");
    assertEquals(List.of(0, 1, 2, 3), home.cardsInView(0, 1, 2, 3));
    assertTrue(home.isNextEnabled(), "\"Next\" can be pressed");

    home.pressNext();

    assertEquals(List.of(1, 2, 3, 4), home.cardsInView(1, 2, 3, 4));
    assertTrue(home.isPreviousEnabled(), "\"Previous\" is enabled");
  }

  @Test
  @DisplayName("On the desktop the slider stops at its last card")
  void onTheDesktopTheSliderStopsAtItsLastCard() {
    HomePage home = HomePage.navigateTo(page);
    assertTrue(home.showSlider(), "the homepage shows the product slider");

    for (int press = 1; press <= 4; press++) {
      assertTrue(home.isNextEnabled(), "\"Next\" can be pressed a " + press + ". time");
      home.pressNext();
      Integer[] expected = IntStream.range(press, press + 4).boxed().toArray(Integer[]::new);
      assertEquals(List.of(expected), home.cardsInView(expected));
    }

    assertEquals(List.of(4, 5, 6, 7), home.cardsInView(4, 5, 6, 7));
    assertTrue(home.isNextDisabled(), "\"Next\" is disabled at the last card");
  }

  /** All cards on one line, left to right in slider order. */
  static void assertSideBySide(List<Position> positions) {
    double top = positions.getFirst().top();
    for (int i = 0; i < positions.size(); i++) {
      Position position = positions.get(i);
      assertTrue(Math.abs(position.top() - top) <= 1, "card " + i + " stands in the first line");
      if (i > 0) {
        assertTrue(
            position.left() > positions.get(i - 1).left(),
            "card " + i + " stands right of card " + (i - 1));
      }
    }
  }
}
