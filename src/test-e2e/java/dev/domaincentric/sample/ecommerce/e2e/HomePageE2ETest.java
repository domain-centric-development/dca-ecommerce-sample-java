package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The home page a visitor sees on arriving at the shop's address. */
@DisplayName("Home Page E2E Tests")
class HomePageE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Home page welcomes the visitor")
  void homePageWelcomesTheVisitorWithWhatTheShopSells() {
    HomePage home = HomePage.navigateTo(page);

    assertEquals("Welcome to domaincentric.commerce", home.heading());
    assertEquals(
        "Books, modelling supplies and hexagon merchandise for people who draw boundaries",
        home.subtitle());
    assertEquals(
        "Everything this architecture is made of: the books it grew out of, the supplies a design"
            + " workshop runs on, and hexagons for your desk.",
        home.description());
  }
}
