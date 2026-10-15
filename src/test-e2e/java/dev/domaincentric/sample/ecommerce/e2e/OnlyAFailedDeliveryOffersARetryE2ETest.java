package dev.domaincentric.sample.ecommerce.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeEventsPage;
import dev.domaincentric.sample.ecommerce.e2e.pages.BackofficeLoginPage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The event log offers "Retry failed listener" only where a retry does something. */
@DisplayName("Only a failed delivery offers a retry E2E Tests")
class OnlyAFailedDeliveryOffersARetryE2ETest extends BaseE2ETest {

  /** The demo operator credentials of a development run. */
  private static final String OPERATOR_USERNAME = "admin";

  private static final String OPERATOR_PASSWORD = "admin";

  @Test
  @DisplayName("A delivery under way offers no retry")
  void aDeliveryStillUnderWayOffersNoRetry() {
    Optional<HeldDelivery> delivery = ShopUnderTest.heldDelivery();
    assumeTrue(
        delivery.isPresent(),
        "the shop was started elsewhere (e2e.baseUrl): it carries no listener that holds a delivery");
    UUID published = delivery.get().publish();
    String eventId = published.toString();
    try {
      BackofficeLoginPage.navigateTo(page)
          .fillCredentials(OPERATOR_USERNAME, OPERATOR_PASSWORD)
          .submit();

      BackofficeEventsPage log = BackofficeEventsPage.navigateTo(page);

      assertEquals("Incomplete", log.statusOfEntryCarrying(eventId), "the entry's badge");
      assertFalse(log.entryCarryingOffersRetry(eventId), "the entry offers a retry");
    } finally {
      delivery.get().release(published);
    }
  }
}
