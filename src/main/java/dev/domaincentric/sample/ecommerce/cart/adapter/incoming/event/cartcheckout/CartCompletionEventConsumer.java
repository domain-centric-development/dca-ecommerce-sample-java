package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.cart.application.completecart.CompleteCartCommand;
import dev.domaincentric.sample.ecommerce.cart.application.completecart.CompleteCartInputPort;
import dev.domaincentric.sample.ecommerce.cart.events.CartCompletionTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer that completes a cart when triggered by a cross-module event.
 *
 * <p>Uses the Interface Inversion pattern: this consumer listens to {@link CartCompletionTrigger},
 * which is defined in the Cart module's {@code events} package. The producing module (Checkout)
 * implements this interface on its {@code CheckoutConfirmedEvent}. This avoids a dependency from
 * Cart to Checkout.
 *
 * <p>Each event is processed in its own transaction, ensuring one-aggregate-per-transaction
 * consistency.
 */
@Component
public class CartCompletionEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(CartCompletionEventConsumer.class);

  private final CompleteCartInputPort completeCartInputPort;

  public CartCompletionEventConsumer(final CompleteCartInputPort completeCartInputPort) {
    this.completeCartInputPort = completeCartInputPort;
  }

  /**
   * Completes the cart when a checkout confirmation event is received.
   *
   * @param event the trigger containing the cart ID to complete
   */
  @ApplicationModuleListener
  void on(final CartCompletionTrigger event) {
    log.info("Completing cart {} after checkout confirmation", event.cartId());
    completeCartInputPort.execute(new CompleteCartCommand(event.cartId()));
  }
}
