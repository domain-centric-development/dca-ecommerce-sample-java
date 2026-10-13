package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A listener failed once on the delivery of an event; the shop retries it on its own and the
 * operator finds the entry completed.
 */
@DisplayName("Automatic redelivery E2E Tests")
class AutomaticRedeliveryE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  /** How long the shop's automatic retries may take to heal a passing failure. */
  private static final Duration HEALING_TIME = Duration.ofSeconds(10);

  @Test
  @DisplayName("Passing failure heals on its own")
  void aDeliveryThatFailedOnceIsCompletedWithoutTheOperator() {
    Optional<FailingOnceDelivery> delivery = ShopUnderTest.failingOnceDelivery();
    assumeTrue(
        delivery.isPresent(),
        "the shop was started elsewhere (e2e.baseUrl): it carries no listener that fails once");
    String eventId = delivery.get().publish().toString();
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();

    BackofficeEventsPage events =
        BackofficeEventsPage.navigateTo(page).reloadUntilEntryCompleted(eventId, HEALING_TIME);

    assertEquals("Completed", events.statusOfEntryCarrying(eventId), "the entry's badge");
    assertTrue(
        events.entryCarryingShowsCompletionDate(eventId), "the entry shows its Completed date");
    assertEquals(
        "Status: COMPLETED, attempts: 2",
        events.payloadFirstLineOfEntryCarrying(eventId),
        "the payload's first line");
  }
}
