package dev.domaincentric.sample.ecommerce.pricing.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;

/**
 * Interface for events that trigger the creation of a price record.
 *
 * <p>This is the consumer-side interface for the Interface Inversion pattern. The Pricing module
 * defines what it needs (a product and the price it starts with), and the producing module (Product
 * Catalog) implements this interface on its event. This way the Pricing module listens to its own
 * interface, avoiding a dependency on the Product module.
 *
 * <p>Extends {@link IntegrationEvent}: delivery is at least once, so the consumer needs the event's
 * identity ({@code eventId()}) to recognise a redelivery, not only the payload.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.event.PriceInitializationEventConsumer
 */
public interface PriceInitializationTrigger extends IntegrationEvent {

  /** The product the price belongs to. */
  String productId();

  /** The price the product starts with. */
  String amount();

  String currency();
}
