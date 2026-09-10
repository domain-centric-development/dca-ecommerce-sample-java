package dev.domaincentric.sample.ecommerce.product.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEventType;
import dev.domaincentric.sample.ecommerce.inventory.events.StockInitializationTrigger;
import dev.domaincentric.sample.ecommerce.pricing.events.PriceInitializationTrigger;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a new product is created.
 *
 * <p>This event is published for cross-module consumption. Internal domain event {@code
 * ProductCreated} is converted to this integration event by {@code ProductCreatedEventPublisher}.
 *
 * <p>Consumers: Pricing context (creates initial price), Inventory context (creates stock level).
 * Both consume their own trigger interface, which this event implements (Interface Inversion): no
 * consumer depends on the Product module, and this module never learns who listens.
 */
@IntegrationEventType(name = "product-created", version = 1)
public record ProductCreatedEvent(
    UUID eventId,
    Instant occurredOn,
    String productId,
    String amount,
    String currency,
    int initialStock)
    implements IntegrationEvent, PriceInitializationTrigger, StockInitializationTrigger {}
