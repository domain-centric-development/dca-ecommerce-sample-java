package dev.domaincentric.sample.ecommerce.e2e;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A delivery to two listeners that fail a different number of times: a test-only integration event,
 * a first listener that throws on its first two deliveries and a second that throws on its first
 * one. The shop offers no way of its own to make a delivery fail, so the shop the suite drives
 * carries this one beside its own listeners: its scan of the shop's base package finds it on the
 * suite's class path.
 */
@Component
public class TwoListenerDelivery {

  /** A text the first listener's entry shows under "Listener", and the second's does not. */
  public static final String FIRST_LISTENER = "firstListener";

  /** A text the second listener's entry shows under "Listener", and the first's does not. */
  public static final String SECOND_LISTENER = "secondListener";

  private final ApplicationEventPublisher publisher;
  private final TransactionTemplate transactions;
  private final Map<UUID, Integer> firstDeliveries = new ConcurrentHashMap<>();
  private final Map<UUID, Integer> secondDeliveries = new ConcurrentHashMap<>();

  public TwoListenerDelivery(
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
        status -> publisher.publishEvent(new SharedHiccup(eventId, Instant.now())));
    return eventId;
  }

  /**
   * Fails on the first two deliveries of an event and handles it from then on.
   *
   * @param event the delivered event
   */
  @ApplicationModuleListener
  public void firstListener(final SharedHiccup event) {
    if (firstDeliveries.merge(event.eventId(), 1, Integer::sum) <= 2) {
      throw new IllegalStateException("first listener's hiccup");
    }
  }

  /**
   * Fails on the first delivery of an event and handles it from then on.
   *
   * @param event the delivered event
   */
  @ApplicationModuleListener
  public void secondListener(final SharedHiccup event) {
    if (secondDeliveries.merge(event.eventId(), 1, Integer::sum) <= 1) {
      throw new IllegalStateException("second listener's hiccup");
    }
  }
}
