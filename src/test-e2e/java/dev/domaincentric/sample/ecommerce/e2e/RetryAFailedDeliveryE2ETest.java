package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A delivery used up its five attempts; once its cause is fixed the operator retries it and finds
 * the entry completed.
 */
@DisplayName("Retry a failed delivery E2E Tests")
class RetryAFailedDeliveryE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  private static final String EXHAUSTED = "Status: FAILED, attempts: 5";

  /** How long the shop's automatic retries may take to use up their attempts. */
  private static final Duration EXHAUSTION_TIME = Duration.ofSeconds(15);

  /** How long the retried delivery may take to run. */
  private static final Duration DELIVERY_TIME = Duration.ofSeconds(10);

  @Test
  @DisplayName("Retry completes a failed delivery")
  void aRetriedDeliveryWhoseCauseIsFixedIsCompleted() {
    Optional<FailingUntilFixedDelivery> delivery = ShopUnderTest.failingUntilFixedDelivery();
    assumeTrue(
        delivery.isPresent(),
        "the shop was started elsewhere (e2e.baseUrl): it carries no listener that fails until fixed");
    UUID published = delivery.get().publish();
    String eventId = published.toString();
    BackofficeLoginPage.navigateTo(page)
        .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
        .submit();
    BackofficeEventsPage before =
        BackofficeEventsPage.navigateTo(page)
            .reloadUntilPayloadFirstLine(eventId, EXHAUSTED, EXHAUSTION_TIME);
    assertEquals(
        EXHAUSTED, before.payloadFirstLineOfEntryCarrying(eventId), "the attempts used up");
    delivery.get().fix(published);
    int completedBefore = before.getCompletedCount();
    int incompleteBefore = before.getIncompleteCount();

    BackofficeEventsPage after = before.retryEntryCarrying(eventId);

    assertEquals("/backoffice/events", after.shownPath(), "the page the retry opens");
    after.reloadUntilEntryCompleted(eventId, DELIVERY_TIME);
    assertEquals("Completed", after.statusOfEntryCarrying(eventId), "the entry's badge");
    assertTrue(
        after.entryCarryingShowsCompletionDate(eventId), "the entry shows its Completed date");
    assertEquals(completedBefore + 1, after.getCompletedCount(), "the Completed count");
    assertEquals(incompleteBefore - 1, after.getIncompleteCount(), "the Incomplete count");
  }
}
