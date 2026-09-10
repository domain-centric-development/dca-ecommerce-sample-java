package dev.domaincentric.sample.ecommerce.inventory.events;

/**
 * Interface for events that trigger the creation of a stock level.
 *
 * <p>This is the consumer-side interface for the Interface Inversion pattern. The Inventory module
 * defines what it needs (a product and the quantity it starts with), and the producing module
 * (Product Catalog) implements this interface on its event. This way the Inventory module listens
 * to its own interface, avoiding a dependency on the Product module.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event.StockInitializationEventConsumer
 */
public interface StockInitializationTrigger {

  /** The product the stock level belongs to. */
  String productId();

  /** The quantity the product starts with. */
  int initialStock();
}
