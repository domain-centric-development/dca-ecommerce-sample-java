package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.events.PriceInitializationTrigger;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The price initialization consumer turns the trigger into a command carrying the event's id. */
class PriceInitializationEventConsumerTest {

  @Test
  @DisplayName("The trigger becomes a price command carrying the event id")
  void theCommandCarriesTheEventId() {
    final List<SetProductPriceCommand> executed = new ArrayList<>();
    final PriceInitializationEventConsumer consumer =
        new PriceInitializationEventConsumer(
            command -> {
              executed.add(command);
              return null;
            });
    final UUID eventId = UUID.randomUUID();

    consumer.on(new Trigger(eventId, Instant.now(), "product-1", "9.99", "EUR"));

    assertEquals(
        List.of(new SetProductPriceCommand("product-1", new BigDecimal("9.99"), "EUR", eventId)),
        executed);
  }

  private record Trigger(
      UUID eventId, Instant occurredOn, String productId, String amount, String currency)
      implements PriceInitializationTrigger {}
}
