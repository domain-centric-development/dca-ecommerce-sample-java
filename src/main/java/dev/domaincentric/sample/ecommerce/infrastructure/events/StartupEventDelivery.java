package dev.domaincentric.sample.ecommerce.infrastructure.events;

import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.core.EventPublicationRegistry;
import org.springframework.stereotype.Component;

/**
 * The last step of the start: waits until the events the start-up caused have been delivered —
 * delivered, or failed and left to recovery.
 *
 * <p>Only what this start published counts: a publication an earlier process left behind is
 * recovery's, not the start's.
 *
 * <p>A seeded product is on sale only once Pricing and Inventory have provisioned it from its
 * event, after the seeder's transaction, on another thread. A shop that answered before then would
 * list products without price or stock. Runs after every other {@link ApplicationRunner}; the .NET
 * twin's seeder waits on its outbox the same way.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class StartupEventDelivery implements ApplicationRunner {

  private final EventPublicationRegistry registry;
  private final Duration timeout;
  private final Instant startedAt = Instant.now();

  public StartupEventDelivery(
      final EventPublicationRegistry registry,
      @Value("${dca.events.startup-delivery-timeout:PT30S}") final Duration timeout) {
    this.registry = registry;
    this.timeout = timeout;
  }

  @Override
  public void run(final ApplicationArguments args) {
    final Instant deadline = Instant.now().plus(timeout);
    while (registry.findIncompletePublications().stream().anyMatch(this::onItsWay)) {
      if (Instant.now().isAfter(deadline)) {
        throw new IllegalStateException("Start-up events still undelivered after " + timeout);
      }
      try {
        Thread.sleep(25);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException("Interrupted while waiting for start-up events", e);
      }
    }
  }

  private boolean onItsWay(final EventPublication publication) {
    return publication.getStatus() != EventPublication.Status.FAILED
        && !publication.getPublicationDate().isBefore(startedAt);
  }
}
