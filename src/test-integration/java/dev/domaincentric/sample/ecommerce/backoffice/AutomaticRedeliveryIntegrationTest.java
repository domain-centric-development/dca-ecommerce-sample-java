package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsResult;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A delivery whose listener fails every time, read through the event log's input port in the wired
 * application once the shop's automatic retries, with the shipped retry policy, have stopped.
 */
@SpringBootTest(
    classes = {
      EcommerceSampleApplication.class,
      AutomaticRedeliveryIntegrationTest.Listeners.class
    })
@DisplayName("Automatic redelivery")
class AutomaticRedeliveryIntegrationTest {

  private static final String EXHAUSTED = "Status: FAILED, attempts: 5";

  /** How long the retries may take to use up their attempts. */
  private static final long EXHAUSTION_TIMEOUT_MILLIS = 15_000;

  /** Longer than the backoff a sixth attempt would wait for after the fifth. */
  private static final long PAST_THE_NEXT_BACKOFF_MILLIS = 5_000;

  @Autowired private GetEventPublicationsInputPort getEventPublications;
  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager transactions;

  @Test
  @DisplayName("Exhausted delivery stays incomplete")
  void aDeliveryFailingEveryTimeStaysFailedAfterFiveAttempts() throws Exception {
    final UUID eventId = publishAlwaysFailing();

    final EventPublicationSummary entry = entryAfterTheRetriesStopped(eventId);

    assertThat(entry.isCompleted()).as("the entry is completed").isFalse();
    assertThat(entry.completionDate()).as("the entry's completion date").isNull();
    assertThat(firstLine(entry)).isEqualTo(EXHAUSTED);
  }

  @Test
  @DisplayName("Exhausted delivery counts as incomplete")
  void anExhaustedDeliveryIsCountedUnderIncomplete() throws Exception {
    final UUID eventId = publishAlwaysFailing();
    entryAfterTheRetriesStopped(eventId);

    final GetEventPublicationsResult result =
        getEventPublications.execute(new GetEventPublicationsQuery());

    final long incomplete =
        result.entries().stream().filter(summary -> !summary.isCompleted()).count();
    assertThat(result.incompleteCount()).isEqualTo(incomplete);
    assertThat(result.completedCount()).isEqualTo(result.entries().size() - incomplete);
    assertThat(result.entries())
        .filteredOn(summary -> summary.serializedEvent().contains(eventId.toString()))
        .singleElement()
        .satisfies(summary -> assertThat(summary.isCompleted()).isFalse());
    assertThat(result.incompleteCount()).isPositive();
  }

  private UUID publishAlwaysFailing() {
    final UUID eventId = UUID.randomUUID();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> publisher.publishEvent(new LastingFailure(eventId, Instant.now())));
    return eventId;
  }

  /** Waits until the entry has used up its attempts, then once more past the next backoff. */
  private EventPublicationSummary entryAfterTheRetriesStopped(final UUID eventId)
      throws InterruptedException {
    final long deadline = System.currentTimeMillis() + EXHAUSTION_TIMEOUT_MILLIS;
    while (true) {
      final Optional<EventPublicationSummary> entry = entryCarrying(eventId);
      final boolean exhausted = entry.map(e -> firstLine(e).equals(EXHAUSTED)).orElse(false);
      if (exhausted || System.currentTimeMillis() > deadline) {
        assertThat(entry.map(AutomaticRedeliveryIntegrationTest::firstLine))
            .as("the delivery's attempts used up")
            .contains(EXHAUSTED);
        break;
      }
      Thread.sleep(100);
    }
    Thread.sleep(PAST_THE_NEXT_BACKOFF_MILLIS);
    return entryCarrying(eventId).orElseThrow();
  }

  private Optional<EventPublicationSummary> entryCarrying(final UUID eventId) {
    return getEventPublications.execute(new GetEventPublicationsQuery()).entries().stream()
        .filter(entry -> entry.serializedEvent().contains(eventId.toString()))
        .findFirst();
  }

  private static String firstLine(final EventPublicationSummary entry) {
    return entry.serializedEvent().split("\n", 2)[0];
  }

  /** An event whose one listener fails on every delivery. */
  public record LastingFailure(UUID eventId, Instant occurredOn) implements IntegrationEvent {}

  static class AlwaysFailingListener {

    @ApplicationModuleListener
    public void deliver(final LastingFailure event) {
      throw new IllegalStateException("lasting failure");
    }
  }

  @TestConfiguration
  static class Listeners {

    @Bean
    AlwaysFailingListener alwaysFailingListener() {
      return new AlwaysFailingListener();
    }
  }
}
