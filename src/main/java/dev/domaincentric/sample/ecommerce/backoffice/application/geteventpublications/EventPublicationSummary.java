package dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications;

import dev.domaincentric.sample.ecommerce.backoffice.application.shared.EventPublicationLogStore.EventPublicationEntry;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Application-layer summary of a single event publication, used as the {@link
 * GetEventPublicationsResult} payload.
 *
 * <p>Mirrors the fields the view consumes, decoupling the use-case API from the {@link
 * EventPublicationEntry} read schema owned by the store.
 *
 * @param id unique identifier of the publication
 * @param eventType fully qualified class name of the event
 * @param serializedEvent JSON-serialized event payload
 * @param listenerId fully qualified listener method that handles this event
 * @param publicationDate when the event was published
 * @param completionDate when the event was completed (null if still incomplete)
 * @param failed whether the delivery has used up its attempts — the one an operator can retry
 */
public record EventPublicationSummary(
    UUID id,
    String eventType,
    String serializedEvent,
    String listenerId,
    Instant publicationDate,
    @Nullable Instant completionDate,
    boolean failed) {

  /**
   * Maps a store entry to its application-layer summary.
   *
   * @param entry the store-owned entry
   * @return the application-layer summary
   */
  public static EventPublicationSummary from(final EventPublicationEntry entry) {
    return new EventPublicationSummary(
        entry.id(),
        entry.eventType(),
        entry.serializedEvent(),
        entry.listenerId(),
        entry.publicationDate(),
        entry.completionDate(),
        entry.failed());
  }

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
