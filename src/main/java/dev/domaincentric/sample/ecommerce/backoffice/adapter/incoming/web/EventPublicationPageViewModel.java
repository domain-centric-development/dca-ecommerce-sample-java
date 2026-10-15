package dev.domaincentric.sample.ecommerce.backoffice.adapter.incoming.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsResult;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page-specific ViewModel for the event publication log page.
 *
 * <p>Transforms use case results into display-friendly values for the Pug template. All fields are
 * primitives or simple strings — no domain objects leak into the view.
 *
 * @param totalEvents total number of event publications
 * @param completedCount number of successfully processed events
 * @param incompleteCount number of events still pending processing
 * @param events list of individual event items for display
 */
public record EventPublicationPageViewModel(
    int totalEvents,
    int completedCount,
    int incompleteCount,
    List<EventPublicationItemViewModel> events) {

  private static final Logger LOG = LoggerFactory.getLogger(EventPublicationPageViewModel.class);

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

  private static final ObjectMapper PRETTY_MAPPER =
      new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Creates a ViewModel from the use case result.
   *
   * @param result the use case result
   * @return page-specific ViewModel
   */
  public static EventPublicationPageViewModel from(final GetEventPublicationsResult result) {
    final List<EventPublicationItemViewModel> items =
        result.entries().stream().map(EventPublicationPageViewModel::toItemViewModel).toList();

    return new EventPublicationPageViewModel(
        result.totalCount(), result.completedCount(), result.incompleteCount(), items);
  }

  private static EventPublicationItemViewModel toItemViewModel(
      final EventPublicationSummary summary) {
    return new EventPublicationItemViewModel(
        summary.id().toString(),
        summary.shortEventType(),
        summary.eventType(),
        prettyPrintJson(summary.serializedEvent()),
        summary.listenerId(),
        DATE_FORMATTER.format(summary.publicationDate()),
        summary.completionDate() != null ? DATE_FORMATTER.format(summary.completionDate()) : null,
        summary.isCompleted(),
        summary.isCompleted() ? "Completed" : "Incomplete",
        summary.failed());
  }

  /**
   * Individual event publication item for display.
   *
   * @param id event publication ID
   * @param shortEventType simple class name of the event
   * @param fullEventType fully qualified class name of the event
   * @param serializedEvent JSON payload of the event
   * @param listenerId the listener that handles this event
   * @param publicationDate formatted publication timestamp
   * @param completionDate formatted completion timestamp (null if incomplete)
   * @param completed whether the event has been processed
   * @param statusLabel display label for the status
   * @param failed whether the delivery has used up its attempts and offers a retry
   */
  public record EventPublicationItemViewModel(
      String id,
      String shortEventType,
      String fullEventType,
      String serializedEvent,
      String listenerId,
      String publicationDate,
      String completionDate,
      boolean completed,
      String statusLabel,
      boolean failed) {}

  private static String prettyPrintJson(final String json) {
    if (json == null || json.isBlank()) {
      return json;
    }
    try {
      final Object parsed = PRETTY_MAPPER.readValue(json, Object.class);
      return PRETTY_MAPPER.writeValueAsString(parsed);
    } catch (final Exception ex) {
      LOG.debug("Could not pretty-print event payload: {}", ex.getMessage());
      return json;
    }
  }
}
