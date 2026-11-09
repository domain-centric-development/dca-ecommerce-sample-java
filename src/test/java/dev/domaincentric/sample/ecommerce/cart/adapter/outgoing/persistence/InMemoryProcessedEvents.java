package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ProcessedEvents;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fake of {@link ProcessedEvents} for tests without a database, keyed by effect and event
 * id as the table is.
 */
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
