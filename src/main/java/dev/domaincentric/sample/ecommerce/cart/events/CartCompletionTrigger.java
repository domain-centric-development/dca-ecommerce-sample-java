package dev.domaincentric.sample.ecommerce.cart.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;

/**
 * Interface for events that trigger cart completion.
 *
 * <p>This is the consumer-side interface for the Interface Inversion pattern. The Cart module
 * defines what it needs (a cart ID), and the producing module (Checkout) implements this interface
 * on its event. This way the Cart module listens to its own interface, avoiding a dependency on the
 * Checkout module.
 *
 * <p>Extends {@link IntegrationEvent}: delivery is at least once, so the consumer needs the event's
 * identity ({@code eventId()}) to recognise a redelivery, not only the payload.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event.cartcheckout.CartCompletionEventConsumer
 */
public interface CartCompletionTrigger extends IntegrationEvent {

  /** The cart ID to complete. */
  String cartId();

  String sessionId();

  java.util.List<String> purchasedPositions();
}
