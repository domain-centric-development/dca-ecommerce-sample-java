package dev.domaincentric.sample.ecommerce.e2e;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * A delivery still under way: a test-only integration event and the one listener that holds its
 * delivery until the test releases it, so the delivery has neither completed nor failed while the
 * test looks at it. The shop the suite drives carries this one beside its own listeners: its scan
 * of the shop's base package finds it on the suite's class path.
 */
@Component
public class HeldDelivery {

  /** How long a delivery is held at most, should a test never release it. */
  private static final Duration LONGEST_HOLD = Duration.ofSeconds(30);

  private final ApplicationEventPublisher publisher;
  private final TransactionTemplate transactions;
  private final Map<UUID, CountDownLatch> held = new ConcurrentHashMap<>();

  public HeldDelivery(
      final ApplicationEventPublisher publisher, final PlatformTransactionManager transactions) {
    this.publisher = publisher;
    this.transactions = new TransactionTemplate(transactions);
  }

  /**
   * Publishes a new event in a committed transaction; its delivery is held until released.
   *
   * @return the id the event's payload carries
   */
  public UUID publish() {
    final UUID eventId = UUID.randomUUID();
    held.put(eventId, new CountDownLatch(1));
    transactions.executeWithoutResult(
        status -> publisher.publishEvent(new HeldBack(eventId, Instant.now())));
    return eventId;
  }

  /**
   * Releases the held delivery: the listener handles the event and the delivery completes.
   *
   * @param eventId the id the event's payload carries
   */
  public void release(final UUID eventId) {
    held.computeIfAbsent(eventId, id -> new CountDownLatch(1)).countDown();
  }

  /**
   * Holds the delivery until the test releases it.
   *
   * @param event the delivered event
   * @throws InterruptedException when the shop stops while the delivery is held
   */
  @ApplicationModuleListener
  public void deliver(final HeldBack event) throws InterruptedException {
    held.computeIfAbsent(event.eventId(), id -> new CountDownLatch(1))
        .await(LONGEST_HOLD.toMillis(), TimeUnit.MILLISECONDS);
  }
}
