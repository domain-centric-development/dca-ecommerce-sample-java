package dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelInputPort;
import dev.domaincentric.sample.ecommerce.inventory.events.StockInitializationTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer that creates the stock level when a product is created.
 *
 * <p>Uses the Interface Inversion pattern: this consumer listens to {@link
 * StockInitializationTrigger}, which is defined in the Inventory module's {@code events} package.
 * The producing module (Product Catalog) implements this interface on its {@code
 * ProductCreatedEvent}. This avoids a dependency from Inventory to Product.
 *
 * <p>Idempotent: setting the same figure twice has the same effect, so a redelivery is harmless.
 */
@Component
public class StockInitializationEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(StockInitializationEventConsumer.class);

  private final SetStockLevelInputPort setStockLevelInputPort;

  public StockInitializationEventConsumer(final SetStockLevelInputPort setStockLevelInputPort) {
    this.setStockLevelInputPort = setStockLevelInputPort;
  }

  /**
   * Creates the initial stock level for a newly created product.
   *
   * @param event the trigger carrying the product and its initial quantity
   */
  @ApplicationModuleListener
  void on(final StockInitializationTrigger event) {
    log.info("Initializing stock for product {}", event.productId());

    setStockLevelInputPort.execute(
        new SetStockLevelCommand(event.productId(), event.initialStock()));
  }
}
