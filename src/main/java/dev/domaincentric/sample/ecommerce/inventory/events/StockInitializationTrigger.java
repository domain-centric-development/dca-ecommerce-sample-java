package dev.domaincentric.sample.ecommerce.inventory.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;

/**
 * Interface for events that trigger the creation of a stock level.
 *
 * <p>This is the consumer-side interface for the Interface Inversion pattern. The Inventory module
 * defines what it needs (a product and the quantity it starts with), and the producing module
 * (Product Catalog) implements this interface on its event. This way the Inventory module listens
 * to its own interface, avoiding a dependency on the Product module.
 *
 * <p>Extends {@link IntegrationEvent}: delivery is at least once, so the consumer needs the event's
 * identity ({@code eventId()}) to recognise a redelivery, not only the payload.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.inventory.adapter.incoming.event.StockInitializationEventConsumer
 */
public interface StockInitializationTrigger extends IntegrationEvent {

  /** The product the stock level belongs to. */
  String productId();

  /** The quantity the product starts with. */
  int initialStock();
}
