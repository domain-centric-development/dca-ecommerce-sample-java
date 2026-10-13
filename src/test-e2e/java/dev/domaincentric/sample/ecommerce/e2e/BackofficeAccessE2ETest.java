package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The back office opens only after an operator login. */
@DisplayName("Backoffice Access E2E Tests")
class BackofficeAccessE2ETest extends BaseE2ETest {

  @Test
  @DisplayName("Event log sends a visitor to the login")
  void eventLogSendsAVisitorWithoutALoginToTheBackofficeLoginPage() {
    navigateTo("/backoffice/events");

    assertEquals("/backoffice/login", getCurrentPath());
  }
}
