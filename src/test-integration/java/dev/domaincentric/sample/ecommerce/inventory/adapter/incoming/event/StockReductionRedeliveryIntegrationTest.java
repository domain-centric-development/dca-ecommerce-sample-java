package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.infrastructure.EcommerceSampleApplication;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockInputPort;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger.OrderLineItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A checkout confirmation is delivered at least once: the stock consumer, with the wired use case
 * and its record of produced effects behind it, reduces stock once for an event it receives twice.
 */
@SpringBootTest(classes = EcommerceSampleApplication.class)
class StockReductionRedeliveryIntegrationTest {

  private static final int INITIAL_STOCK = 50;

  @Autowired private ReduceStockInputPort reduceStock;

  @Autowired private StockLevelRepository stockLevels;

  private StockReductionEventConsumer consumer;

  /**
   * The consumer as the application wires it, called directly: the listener annotation hands a call
   * through the Spring proxy to another thread, and this test asserts the state after it.
   */
  @BeforeEach
  void wireConsumer() {
    consumer = new StockReductionEventConsumer(reduceStock);
  }

  @Test
  @DisplayName("The same checkout confirmation delivered twice reduces stock once")
  void aRedeliveredConfirmationReducesStockOnce() {
    final ProductId product = ProductId.generate();
    stockLevels.save(StockLevel.create(product, INITIAL_STOCK));
    final Trigger confirmation =
        new Trigger(UUID.randomUUID(), Instant.now(), List.of(new OrderLineItem(product, 3)));

    consumer.on(confirmation);
    consumer.on(confirmation);

    assertEquals(INITIAL_STOCK - 3, available(product));
  }

  @Test
  @DisplayName("Two confirmations with the same line items both reduce stock")
  void twoConfirmationsReduceStockTwice() {
    final ProductId product = ProductId.generate();
    stockLevels.save(StockLevel.create(product, INITIAL_STOCK));
    final List<OrderLineItem> lines = List.of(new OrderLineItem(product, 3));

    consumer.on(new Trigger(UUID.randomUUID(), Instant.now(), lines));
    consumer.on(new Trigger(UUID.randomUUID(), Instant.now(), lines));

    assertEquals(INITIAL_STOCK - 6, available(product));
  }

  private int available(final ProductId product) {
    return stockLevels.findByProductId(product).orElseThrow().availableQuantity().value();
  }

  private record Trigger(UUID eventId, Instant occurredOn, List<OrderLineItem> orderLineItems)
      implements StockReductionTrigger {}
}
