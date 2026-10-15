package dev.domaincentric.sample.ecommerce.backoffice.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Store;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Store for the Spring Modulith event publication log.
 *
 * <p>Append-only operational data with no aggregate lifecycle — Spring Modulith manages writes;
 * this port only reads. Modelled as a {@link Store} (not a {@code Repository}) because the stored
 * entries have no own identity-based load/mutate/save lifecycle.
 */
public interface EventPublicationLogStore extends Store {

  /**
   * Retrieves all event publications ordered by publication date descending (newest first).
   *
   * @return all event publication entries
   */
  List<EventPublicationEntry> findAll();

  /**
   * A single entry from the event publication log.
   *
   * @param id unique identifier of the publication
   * @param eventType fully qualified class name of the event
   * @param serializedEvent JSON-serialized event payload
   * @param listenerId fully qualified listener method that handles this event
   * @param publicationDate when the event was published
   * @param completionDate when the event was completed (null if still incomplete)
   * @param failed whether the delivery has used up its attempts and no automatic retry is left
   */
  record EventPublicationEntry(
      UUID id,
      String eventType,
      String serializedEvent,
      String listenerId,
      Instant publicationDate,
      @Nullable Instant completionDate,
      boolean failed) {

    /**
     * Whether this event publication has been completed.
     *
     * @return true if the event was successfully processed
     */
    public boolean isCompleted() {
      return completionDate != null;
    }

    /**
     * Returns the simple class name of the event type (without package prefix).
     *
     * @return short event type name
     */
    public String shortEventType() {
      final int lastDot = eventType.lastIndexOf('.');
      return lastDot >= 0 ? eventType.substring(lastDot + 1) : eventType;
    }
  }
}
