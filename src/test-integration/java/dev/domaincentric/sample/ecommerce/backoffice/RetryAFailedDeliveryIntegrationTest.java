package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.ReplayFailedPublicationCommand;
import dev.domaincentric.sample.ecommerce.backoffice.application.replayfailedpublication.ReplayFailedPublicationInputPort;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
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
 * An operator's retry of a delivery that used up its attempts, through its input port in the wired
 * application, read back through the event log's input port.
 */
@SpringBootTest(
    classes = {
      EcommerceSampleApplication.class,
      RetryAFailedDeliveryIntegrationTest.Listeners.class
    })
@DisplayName("Retry a failed delivery")
class RetryAFailedDeliveryIntegrationTest {

  private static final String EXHAUSTED = "Status: FAILED, attempts: 5";

  /** How long the automatic retries may take to use up their attempts. */
  private static final long EXHAUSTION_TIMEOUT_MILLIS = 15_000;

  /** How long a retried delivery may take to run. */
  private static final long DELIVERY_TIMEOUT_MILLIS = 10_000;

  @Autowired private GetEventPublicationsInputPort getEventPublications;
  @Autowired private ReplayFailedPublicationInputPort replayFailedPublication;
  @Autowired private ApplicationEventPublisher publisher;
  @Autowired private PlatformTransactionManager transactions;
  @Autowired private Fixes fixes;

  @Test
  @DisplayName("Retry delivers the original event")
  void aRetriedDeliveryCarriesTheEventItCarriedBefore() throws Exception {
    final UUID eventId = publish(new LoneFailure(eventId(), Instant.now()));
    final EventPublicationSummary failed =
        await(eventId, entry -> firstLine(entry).equals(EXHAUSTED), EXHAUSTION_TIMEOUT_MILLIS);
    fixes.fix(eventId);

    replayFailedPublication.execute(new ReplayFailedPublicationCommand(failed.id()));

    final EventPublicationSummary completed =
        await(eventId, EventPublicationSummary::isCompleted, DELIVERY_TIMEOUT_MILLIS);
    assertThat(content(completed)).isEqualTo(content(failed));
    assertThat(content(completed)).contains(eventId.toString());
  }

  @Test
  @DisplayName("Retry leaves other listeners alone")
  void aRetryKeepsTheCompletionOfTheListenerThatHandledTheEvent() throws Exception {
    final UUID eventId = publish(new SharedFailure(eventId(), Instant.now()));
    final EventPublicationSummary failed =
        await(
            eventId,
            entry ->
                entry.listenerId().contains(FailingUntilFixedListener.class.getSimpleName())
                    && firstLine(entry).equals(EXHAUSTED),
            EXHAUSTION_TIMEOUT_MILLIS);
    final EventPublicationSummary handled =
        await(
            eventId,
            entry ->
                entry.listenerId().contains(HandlingListener.class.getSimpleName())
                    && entry.isCompleted(),
            DELIVERY_TIMEOUT_MILLIS);
    fixes.fix(eventId);

    replayFailedPublication.execute(new ReplayFailedPublicationCommand(failed.id()));

    await(
        eventId,
        entry -> entry.id().equals(failed.id()) && entry.isCompleted(),
        DELIVERY_TIMEOUT_MILLIS);
    final EventPublicationSummary handledAfter =
        entries(eventId, entry -> entry.id().equals(handled.id())).orElseThrow();
    assertThat(handledAfter.completionDate()).isEqualTo(handled.completionDate());
  }

  @Test
  @DisplayName("Failing retry leaves the entry failed")
  void aRetryWhoseListenerStillFailsLeavesTheEntryIncomplete() throws Exception {
    final UUID eventId = publish(new LoneFailure(eventId(), Instant.now()));
    final EventPublicationSummary failed =
        await(eventId, entry -> firstLine(entry).equals(EXHAUSTED), EXHAUSTION_TIMEOUT_MILLIS);

    replayFailedPublication.execute(new ReplayFailedPublicationCommand(failed.id()));

    final EventPublicationSummary retried =
        await(
            eventId,
            entry -> firstLine(entry).equals("Status: FAILED, attempts: 6"),
            DELIVERY_TIMEOUT_MILLIS);
    assertThat(retried.isCompleted()).as("the entry is completed").isFalse();
  }

  private static UUID eventId() {
    return UUID.randomUUID();
  }

  private UUID publish(final IntegrationEvent event) {
    new TransactionTemplate(transactions)
        .executeWithoutResult(status -> publisher.publishEvent(event));
    return event.eventId();
  }

  /** Waits until an entry of the event matches, and fails when none does in time. */
  private EventPublicationSummary await(
      final UUID eventId, final Predicate<EventPublicationSummary> state, final long timeoutMillis)
      throws InterruptedException {
    final long deadline = System.currentTimeMillis() + timeoutMillis;
    Optional<EventPublicationSummary> entry = entries(eventId, state);
    while (entry.isEmpty() && System.currentTimeMillis() < deadline) {
      Thread.sleep(100);
      entry = entries(eventId, state);
    }
    assertThat(entry).as("an entry of the event in the awaited state").isPresent();
    return entry.orElseThrow();
  }

  private Optional<EventPublicationSummary> entries(
      final UUID eventId, final Predicate<EventPublicationSummary> state) {
    return getEventPublications.execute(new GetEventPublicationsQuery()).entries().stream()
        .filter(entry -> entry.serializedEvent().contains(eventId.toString()))
        .filter(state)
        .findFirst();
  }

  private static String firstLine(final EventPublicationSummary entry) {
    return entry.serializedEvent().split("\n", 2)[0];
  }

  /** The event's content, below the line that states the delivery's status. */
  private static String content(final EventPublicationSummary entry) {
    return entry.serializedEvent().split("\n", 2)[1];
  }

  /** An event with one listener, which fails until its cause is fixed. */
  public record LoneFailure(UUID eventId, Instant occurredOn) implements IntegrationEvent {}

  /** An event with two listeners: one handles it, the other fails until its cause is fixed. */
  public record SharedFailure(UUID eventId, Instant occurredOn) implements IntegrationEvent {}

  /** The events whose cause the test fixed. */
  static class Fixes {

    private final Set<UUID> fixed = ConcurrentHashMap.newKeySet();

    void fix(final UUID eventId) {
      fixed.add(eventId);
    }

    void failUnlessFixed(final UUID eventId) {
      if (!fixed.contains(eventId)) {
        throw new IllegalStateException("cause not fixed");
      }
    }
  }

  static class FailingUntilFixedListener {

    private final Fixes fixes;

    FailingUntilFixedListener(final Fixes fixes) {
      this.fixes = fixes;
    }

    @ApplicationModuleListener
    public void deliver(final LoneFailure event) {
      fixes.failUnlessFixed(event.eventId());
    }

    @ApplicationModuleListener
    public void deliver(final SharedFailure event) {
      fixes.failUnlessFixed(event.eventId());
    }
  }

  static class HandlingListener {

    @ApplicationModuleListener
    public void deliver(final SharedFailure event) {
      // handles every delivery
    }
  }

  @TestConfiguration
  static class Listeners {

    @Bean
    Fixes fixes() {
      return new Fixes();
    }

    @Bean
    FailingUntilFixedListener failingUntilFixedListener(final Fixes fixes) {
      return new FailingUntilFixedListener(fixes);
    }

    @Bean
    HandlingListener handlingListener() {
      return new HandlingListener();
    }
  }
}
