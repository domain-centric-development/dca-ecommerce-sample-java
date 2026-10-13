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
 * A delivery that fails once and succeeds from then on: a test-only integration event and the one
 * listener that throws on its first delivery. The shop offers no way of its own to make a delivery
 * fail, so the shop the suite drives carries this one beside its own listeners: its scan of the
 * shop's base package finds it on the suite's class path.
 */
@Component
public class FailingOnceDelivery {

  private final ApplicationEventPublisher publisher;
  private final TransactionTemplate transactions;
  private final Set<UUID> failedOnce = ConcurrentHashMap.newKeySet();

  public FailingOnceDelivery(
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
        status -> publisher.publishEvent(new PassingHiccup(eventId, Instant.now())));
    return eventId;
  }

  /**
   * Fails on the first delivery of an event and handles it from then on.
   *
   * @param event the delivered event
   */
  @ApplicationModuleListener
  public void deliver(final PassingHiccup event) {
    if (failedOnce.add(event.eventId())) {
      throw new IllegalStateException("passing hiccup");
    }
  }

  /** The event whose delivery fails once. */
  public record PassingHiccup(UUID eventId, Instant occurredOn) implements IntegrationEvent {}
}
