package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The operator opens an entry of the event publication log and reads what the event carried. */
@DisplayName("Event Payload E2E Tests")
class EventPayloadE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  @Test
  @DisplayName("Payload can be expanded")
  void theFirstEntrysPayloadShowsWhatTheEventCarried() {
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();
    BackofficeEventsPage events = BackofficeEventsPage.navigateTo(page);
    assertTrue(events.getEventCount() >= 1, "the log lists at least one entry");

    events.expandFirstEventPayload();

    assertTrue(events.isFirstEventPayloadContentVisible(), "the payload is shown");
    assertFalse(events.getFirstEventPayloadContent().isBlank(), "the payload is not empty");
  }
}
