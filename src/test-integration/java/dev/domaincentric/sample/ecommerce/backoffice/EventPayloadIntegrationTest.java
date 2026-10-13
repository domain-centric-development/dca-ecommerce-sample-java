package dev.domaincentric.sample.ecommerce.backoffice;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.EventPublicationSummary;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsInputPort;
import dev.domaincentric.sample.ecommerce.backoffice.application.geteventpublications.GetEventPublicationsQuery;
import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * The payload of an entry of the event publication log, read through its input port in the wired
 * application, against a product the shop's start-up created.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
@DisplayName("Event publication payload")
class EventPayloadIntegrationTest {

  private static final ObjectMapper JSON = new ObjectMapper();

  /** How long the start-up's deliveries may take to complete. */
  private static final long DELIVERY_TIMEOUT_MILLIS = 10_000;

  @Autowired private GetEventPublicationsInputPort getEventPublications;

  @Test
  @DisplayName("Payload starts with status and attempts")
  void aDeliveredProductCreationShowsItsStatusAttemptsAndContent() throws Exception {
    final EventPublicationSummary entry = completedProductCreation();

    final String[] lines = entry.serializedEvent().split("\n", 2);

    assertThat(lines[0]).isEqualTo("Status: COMPLETED, attempts: 1");
    assertThat(lines).hasSize(2);
    final JsonNode content = JSON.readTree(lines[1]);
    final List<String> fields = new ArrayList<>();
    content.fieldNames().forEachRemaining(fields::add);
    assertThat(fields)
        .containsExactlyInAnyOrder(
            "eventId", "occurredOn", "productId", "amount", "currency", "initialStock");
  }

  /** A completed delivery of a product created at start-up, once its listener has completed it. */
  private EventPublicationSummary completedProductCreation() throws InterruptedException {
    final long deadline = System.currentTimeMillis() + DELIVERY_TIMEOUT_MILLIS;
    while (true) {
      final Optional<EventPublicationSummary> completed =
          getEventPublications.execute(new GetEventPublicationsQuery()).entries().stream()
              .filter(entry -> entry.shortEventType().equals("ProductCreatedEvent"))
              .filter(EventPublicationSummary::isCompleted)
              .findFirst();
      if (completed.isPresent() || System.currentTimeMillis() > deadline) {
        assertThat(completed).as("a completed ProductCreatedEvent delivery").isPresent();
        return completed.get();
      }
      Thread.sleep(100);
    }
  }
}
