package dev.domaincentric.sample.ecommerce.e2e;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A delivery that fails until its cause is fixed: a test-only integration event and the one
 * listener that throws on every delivery of an event until the test fixes it. The shop offers no
 * way of its own to make a delivery fail, so the shop the suite drives carries this one beside its
 * own listeners: its scan of the shop's base package finds it on the suite's class path.
 */
@Component
public class FailingUntilFixedDelivery {

  private final ApplicationEventPublisher publisher;
  private final TransactionTemplate transactions;
  private final Set<UUID> fixed = ConcurrentHashMap.newKeySet();

  public FailingUntilFixedDelivery(
      final ApplicationEventPublisher publisher, final PlatformTransactionManager transactions) {
    this.publisher = publisher;
    this.transactions = new TransactionTemplate(transactions);
  }

  /**
   * Publishes a new event in a committed transaction.
   *
   * @return the id the event's payload carries
   */
  public UUID publish() {
    final UUID eventId = UUID.randomUUID();
    transactions.executeWithoutResult(
        status -> publisher.publishEvent(new LastingHiccup(eventId, Instant.now())));
    return eventId;
  }

  /**
   * Fixes the cause: the listener handles the event from now on.
   *
   * @param eventId the id the event's payload carries
   */
  public void fix(final UUID eventId) {
    fixed.add(eventId);
  }

  /**
   * Fails on every delivery of an event until it is fixed.
   *
   * @param event the delivered event
   */
  @ApplicationModuleListener
  public void deliver(final LastingHiccup event) {
    if (!fixed.contains(event.eventId())) {
      throw new IllegalStateException("lasting hiccup");
    }
  }

  /** The event whose delivery fails until it is fixed. */
  public record LastingHiccup(UUID eventId, Instant occurredOn) implements IntegrationEvent {}
}
