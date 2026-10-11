package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ProcessedEvents;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of {@link ProcessedEvents}, keyed by effect and event id — the stand-in
 * for a table with that composite key.
 *
 * <p>Thread-safe and process-local: the record lives as long as the application, like this
 * context's in-memory stores. A deployment that keeps its data across restarts keeps this record in
 * the same store and transaction as the effect it guards.
 */
@Component("cartProcessedEvents")
public class InMemoryProcessedEvents implements ProcessedEvents {

  private final Set<Key> processed = ConcurrentHashMap.newKeySet();

  @Override
  public boolean contains(final String effect, final UUID eventId) {
    return processed.contains(new Key(effect, eventId));
  }

  @Override
  public void markProcessed(final String effect, final UUID eventId) {
    processed.add(new Key(effect, eventId));
  }

  private record Key(String effect, UUID eventId) {}
}
