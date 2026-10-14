package dev.domaincentric.sample.ecommerce.e2e;

import static dev.domaincentric.sample.ecommerce.e2e.TwoListenerDelivery.FIRST_LISTENER;
import static dev.domaincentric.sample.ecommerce.e2e.TwoListenerDelivery.SECOND_LISTENER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * An event with two listeners, the first of which failed twice and the second once before each
 * handled it; the operator finds each entry counting its own listener's attempts.
 */
@DisplayName("Attempts per listener E2E Tests")
class AttemptsPerListenerE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  /** How long the shop's automatic retries may take to heal both listeners. */
  private static final Duration HEALING_TIME = Duration.ofSeconds(10);

  @Test
  @DisplayName("Each listener counts its own attempts")
  void theEntriesOfOneEventCountTheTriesOfTheirOwnListener() {
    Optional<TwoListenerDelivery> delivery = ShopUnderTest.twoListenerDelivery();
    assumeTrue(
        delivery.isPresent(),
        "the shop was started elsewhere (e2e.baseUrl): it carries no listeners that fail");
    String eventId = delivery.get().publish().toString();
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();
    BackofficeEventsPage events =
        BackofficeEventsPage.navigateTo(page)
            .reloadUntilEntryCompleted(eventId, FIRST_LISTENER, HEALING_TIME)
            .reloadUntilEntryCompleted(eventId, SECOND_LISTENER, HEALING_TIME);

    events.expandPayloadOfEntry(eventId, FIRST_LISTENER);
    events.expandPayloadOfEntry(eventId, SECOND_LISTENER);

    assertEquals(
        "Status: COMPLETED, attempts: 3",
        events.payloadFirstLineOfEntry(eventId, FIRST_LISTENER),
        "the first listener's payload line");
    assertEquals(
        "Status: COMPLETED, attempts: 2",
        events.payloadFirstLineOfEntry(eventId, SECOND_LISTENER),
        "the second listener's payload line");
  }
}
