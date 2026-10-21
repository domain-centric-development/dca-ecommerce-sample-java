package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeHeader;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** A back-office page's header is the operator's, not the shopper's. */
@DisplayName("Backoffice Header E2E Tests")
class BackofficeHeaderE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  @Test
  @DisplayName("The back office header offers the operators logout")
  void eventLogHeaderShowsTheShopBackOfficeAndTheOperatorsLogout() {
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();

    BackofficeEventsPage.navigateTo(page);
    BackofficeHeader header = new BackofficeHeader(page);

    assertEquals("Back office", header.section());
    assertEquals("domaincentric.commerce", header.shopName());
    assertEquals("Logout", header.logoutButton());
    assertFalse(header.offers("Login"), "the header offers no Login");
    assertFalse(header.offers("Register"), "the header offers no Register");
    assertFalse(header.offers("Cart"), "the header offers no Cart");
  }
}
