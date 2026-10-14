package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
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
 * An event with two listeners, one failing on every delivery and one handling it at once, read
 * through the event log's input port in the wired application once the automatic retries, with the
 * shipped retry policy, have stopped.
 */
@SpringBootTest(
    classes = {
      EcommerceSampleApplication.class,
      AttemptsPerListenerIntegrationTest.Listeners.class
    })
@DisplayName("Attempts per listener")
class AttemptsPerListenerIntegrationTest {

  private static final String EXHAUSTED = "Status: FAILED, attempts: 5";

  /** How long the retries may take to use up their attempts. */
  private static final long EXHAUSTION_TIMEOUT_MILLIS = 15_000;

  /** Longer than the backoff a sixth attempt would wait for after the fifth. */
  private static final long PAST_THE_NEXT_BACKOFF_MILLIS = 5_000;

  @Autowired private GetEventPublicationsInputPort getEventPublications;
  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager transactions;

  @Test
  @DisplayName("A failing listener leaves the other's count alone")
  void aListenerFailingEveryTimeLeavesTheCountOfTheOneThatHandledTheEventAtOne() throws Exception {
    final UUID eventId = publish();
    awaitTheRetriesStopped(eventId);

    final Optional<EventPublicationSummary> failing = entryOf(eventId, AlwaysFailingListener.class);
    final Optional<EventPublicationSummary> handling = entryOf(eventId, HandlingListener.class);

    assertThat(failing.map(AttemptsPerListenerIntegrationTest::firstLine))
        .as("the failing listener's payload line")
        .contains(EXHAUSTED);
    assertThat(handling.map(AttemptsPerListenerIntegrationTest::firstLine))
        .as("the handling listener's payload line")
        .contains("Status: COMPLETED, attempts: 1");
  }

  private UUID publish() {
    final UUID eventId = UUID.randomUUID();
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> publisher.publishEvent(new FailingForOne(eventId, Instant.now())));
    return eventId;
  }

  /** Waits until the failing listener's entry has used up its attempts, then past the backoff. */
  private void awaitTheRetriesStopped(final UUID eventId) throws InterruptedException {
    final long deadline = System.currentTimeMillis() + EXHAUSTION_TIMEOUT_MILLIS;
    while (!entryOf(eventId, AlwaysFailingListener.class)
            .map(entry -> firstLine(entry).equals(EXHAUSTED))
            .orElse(false)
        && System.currentTimeMillis() < deadline) {
      Thread.sleep(100);
    }
    Thread.sleep(PAST_THE_NEXT_BACKOFF_MILLIS);
  }

  private Optional<EventPublicationSummary> entryOf(final UUID eventId, final Class<?> listener) {
    return getEventPublications.execute(new GetEventPublicationsQuery()).entries().stream()
        .filter(entry -> entry.serializedEvent().contains(eventId.toString()))
        .filter(entry -> entry.listenerId().contains(listener.getSimpleName()))
        .findFirst();
  }

  private static String firstLine(final EventPublicationSummary entry) {
    return entry.serializedEvent().split("\n", 2)[0];
  }

  /** An event with two listeners: one fails on every delivery, the other handles it. */
  public record FailingForOne(UUID eventId, Instant occurredOn) implements IntegrationEvent {}

  static class AlwaysFailingListener {

    @ApplicationModuleListener
    public void deliver(final FailingForOne event) {
      throw new IllegalStateException("lasting failure");
    }
  }

  static class HandlingListener {

    @ApplicationModuleListener
    public void deliver(final FailingForOne event) {}
  }

  @TestConfiguration
  static class Listeners {

    @Bean
    AlwaysFailingListener alwaysFailingListener() {
      return new AlwaysFailingListener();
    }

    @Bean
    HandlingListener handlingListener() {
      return new HandlingListener();
    }
  }
}
