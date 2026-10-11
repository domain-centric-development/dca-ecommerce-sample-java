package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Store;
import java.util.UUID;

/**
 * The integration events this context has already turned into an effect, keyed by the effect and
 * the event's {@code eventId()}.
 *
 * <p>Delivery between contexts is at least once, so the same event can arrive twice. The use case
 * that produces an effect asks this store before it acts and records the pair after the effect, in
 * the same transaction: a redelivery then changes nothing, and a failed attempt leaves no mark and
 * is retried. The key holds the effect as well as the event id because two effects may react to the
 * same event, and each has to happen exactly once.
 *
 * <p>Modelled as a {@link Store}: the entries are records of handling with no lifecycle of their
 * own, not an aggregate.
 */
public interface ProcessedEvents extends Store {

  /**
   * Whether the effect has already been produced for the event with this id.
   *
   * @param effect the stable name of the effect
   * @param eventId the integration event's identity
   * @return {@code true} when the pair was recorded before
   */
  boolean contains(String effect, UUID eventId);

  /**
   * Records that the effect has been produced for the event with this id.
   *
   * @param effect the stable name of the effect
   * @param eventId the integration event's identity
   */
  void markProcessed(String effect, UUID eventId);
}
