package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The operator logs in to the back office and lands on the event publication log. */
@DisplayName("Backoffice Login E2E Tests")
class BackofficeLoginE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  @Test
  @DisplayName("Login opens the event log")
  void loginOpensTheEventPublicationLog() {
    BackofficeEventsPage events =
        BackofficeLoginPage.navigateTo(page)
            .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
            .submit();

    assertEquals("/backoffice/events", events.shownPath());
    assertEquals("Event Publication Log", events.heading());
    assertEquals(List.of("Total Events", "Completed", "Incomplete"), events.counterLabels());
    assertTrue(events.getTotalEvents() >= 0, "the total is not negative");
  }
}
