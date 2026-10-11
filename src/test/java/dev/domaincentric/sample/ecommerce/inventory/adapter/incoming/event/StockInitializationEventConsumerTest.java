package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.events.StockInitializationTrigger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The stock initialization consumer turns the trigger into a command carrying the event's id. */
class StockInitializationEventConsumerTest {

  @Test
  @DisplayName("The trigger becomes a stock level command carrying the event id")
  void theCommandCarriesTheEventId() {
    final List<SetStockLevelCommand> executed = new ArrayList<>();
    final StockInitializationEventConsumer consumer =
        new StockInitializationEventConsumer(
            command -> {
              executed.add(command);
              return null;
            });
    final UUID eventId = UUID.randomUUID();

    consumer.on(new Trigger(eventId, Instant.now(), "product-1", 10));

    assertEquals(List.of(new SetStockLevelCommand("product-1", 10, eventId)), executed);
  }

  private record Trigger(UUID eventId, Instant occurredOn, String productId, int initialStock)
      implements StockInitializationTrigger {}
}
