package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockInputPort;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer that reduces stock when triggered by a cross-module event.
 *
 * <p>Uses the Interface Inversion pattern: this consumer listens to {@link StockReductionTrigger},
 * which is defined in the Inventory module's {@code events} package. The producing module
 * (Checkout) implements this interface on its {@code CheckoutConfirmedEvent}. This avoids a
 * dependency from Inventory to Checkout.
 *
 * <p>Each event is processed in its own transaction, ensuring one-aggregate-per-transaction
 * consistency.
 *
 * <p>Delivery is at least once: the consumer hands the event's {@code eventId()} on in the command,
 * and the use case skips an effect it has already produced for that id, inside its own transaction.
 */
@Component
public class StockReductionEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(StockReductionEventConsumer.class);

  private final ReduceStockInputPort reduceStockInputPort;

  public StockReductionEventConsumer(final ReduceStockInputPort reduceStockInputPort) {
    this.reduceStockInputPort = reduceStockInputPort;
  }

  /**
   * Reduces stock for each line item when a checkout confirmation event is received.
   *
   * @param event the trigger containing order line items
   */
  @ApplicationModuleListener
  void on(final StockReductionTrigger event) {
    log.info(
        "Reducing stock for {} line items after checkout confirmation",
        event.orderLineItems().size());

    event
        .orderLineItems()
        .forEach(
            item -> {
              log.debug(
                  "Reducing stock for product {} by {}", item.productId().value(), item.quantity());
              reduceStockInputPort.execute(
                  new ReduceStockCommand(
                      item.productId().value(), item.quantity(), event.eventId()));
            });
  }
}
