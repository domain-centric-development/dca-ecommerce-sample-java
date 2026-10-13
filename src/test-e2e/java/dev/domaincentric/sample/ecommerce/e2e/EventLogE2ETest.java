package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The operator opens the event publication log and finds what the shop published at start-up. */
@DisplayName("Event Log E2E Tests")
class EventLogE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  /** A type label names the event by its simple class name. */
  private static final String EVENT_NAME = "[A-Z][A-Za-z0-9]*";

  @Test
  @DisplayName("Log lists the events published at start up")
  void theLogListsTheEventsThatStartUpPublished() {
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();

    BackofficeEventsPage events = BackofficeEventsPage.navigateTo(page);

    assertTrue(events.getEventCount() >= 1, "the log lists at least one entry");
    List<String> types = events.getEventTypes();
    assertEquals(events.getEventCount(), types.size(), "every entry shows a type label");
    for (String type : types) {
      assertTrue(type.strip().matches(EVENT_NAME), "the type label names the event: " + type);
    }
  }
}
