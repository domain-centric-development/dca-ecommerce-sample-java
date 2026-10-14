package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockInputPort;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockResult;
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
   * Reduces the stock of every line item of a confirmed checkout, in one command.
   *
   * @param event the trigger containing order line items
   */
  @ApplicationModuleListener
  void on(final StockReductionTrigger event) {
    log.info(
        "Reducing stock for {} line items after checkout confirmation", event.lineItems().size());

    final ReduceStockResult result =
        reduceStockInputPort.execute(
            new ReduceStockCommand(
                event.lineItems().stream()
                    .map(
                        item ->
                            new ReduceStockCommand.Line(item.productId().value(), item.quantity()))
                    .toList(),
                event.eventId()));
    result.lines().stream()
        .filter(line -> !line.success())
        .forEach(
            line ->
                log.warn(
                    "Stock reduction for product {} failed: {} (stock {})",
                    line.productId(),
                    line.failure().orElseThrow(),
                    line.previousStock()));
  }
}
