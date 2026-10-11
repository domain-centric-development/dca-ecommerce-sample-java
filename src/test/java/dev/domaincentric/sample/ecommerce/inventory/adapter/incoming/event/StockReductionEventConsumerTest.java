package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockResult;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger.OrderLineItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The stock reduction consumer turns the sale into one command that carries the event's id. */
class StockReductionEventConsumerTest {

  @Test
  @DisplayName("All lines become one reduction command carrying the event id")
  void allLinesInOneCommandWithTheEventId() {
    final List<ReduceStockCommand> executed = new ArrayList<>();
    final StockReductionEventConsumer consumer =
        new StockReductionEventConsumer(
            command -> {
              executed.add(command);
              return ReduceStockResult.reduced(List.of());
            });
    final UUID eventId = UUID.randomUUID();
    final ProductId first = ProductId.of("product-1");
    final ProductId second = ProductId.of("product-2");

    consumer.on(
        new Trigger(
            eventId,
            Instant.now(),
            List.of(new OrderLineItem(first, 2), new OrderLineItem(second, 1))));

    assertEquals(
        List.of(
            new ReduceStockCommand(
                List.of(
                    new ReduceStockCommand.Line(first.value(), 2),
                    new ReduceStockCommand.Line(second.value(), 1)),
                eventId)),
        executed);
  }

  private record Trigger(UUID eventId, Instant occurredOn, List<OrderLineItem> orderLineItems)
      implements StockReductionTrigger {}
}
