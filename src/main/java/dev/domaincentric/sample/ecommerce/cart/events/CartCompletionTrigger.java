package dev.domaincentric.sample.ecommerce.cart.events;

/**
 * Interface for events that trigger cart completion.
 *
 * <p>This is the consumer-side interface for the Interface Inversion pattern. The Cart module
 * defines what it needs (a cart ID), and the producing module (Checkout) implements this interface
 * on its event. This way the Cart module listens to its own interface, avoiding a dependency on the
 * Checkout module.
 *
 * @see
 *     dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event.cartcheckout.CartCompletionEventConsumer
 */
public interface CartCompletionTrigger {

  /** The cart ID to complete. */
  String cartId();
}
