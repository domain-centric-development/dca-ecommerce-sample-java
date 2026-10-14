package dev.domaincentric.sample.ecommerce.e2e;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import java.time.Instant;
import java.util.UUID;

/**
 * The event two listeners receive, published by {@link TwoListenerDelivery}. A class of its own,
 * not nested: the event log labels an event by its simple class name, and so labels this one like
 * every event of the shop.
 *
 * @param eventId the id the payload carries
 * @param occurredOn when it was published
 */
public record SharedHiccup(UUID eventId, Instant occurredOn) implements IntegrationEvent {}
