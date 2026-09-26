package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.e2e.pages.HomePage;
import dev.domaincentric.sample.ecommerce.e2e.pages.ProductCatalogPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.SiteHeader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The header every storefront page shares, followed from a page deep in the shop. */
@DisplayName("Site Header E2E Tests")
class SiteHeaderE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Logo leads home")
  void logoOnAProductPageLeadsToTheHomePage() {
    ProductCatalogPage.navigateTo(page).viewProductNamed("Domain-Driven Design");
    SiteHeader header = new SiteHeader(page);
    assertEquals("domaincentric.commerce", header.logoName());

    HomePage home = header.followLogo();

    assertEquals("/", getCurrentPath());
    assertEquals("Welcome to domaincentric.commerce", home.heading());
  }
}
