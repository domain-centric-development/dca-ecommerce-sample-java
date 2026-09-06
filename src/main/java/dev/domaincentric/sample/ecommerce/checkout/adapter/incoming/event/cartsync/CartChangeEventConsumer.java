package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent;
import dev.domaincentric.sample.ecommerce.checkout.application.synccheckoutwithcart.SyncCheckoutWithCartCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.synccheckoutwithcart.SyncCheckoutWithCartInputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for cart content changes in the Checkout context.
 *
 * <p><b>Cross-Context Integration:</b> This listener enables eventual consistency between the Cart
 * and Checkout bounded contexts. When the cart contents change during an active checkout session,
 * the session's line items are synchronized.
 *
 * <p>Consumes the consolidated {@link CartContentsChangedEvent} integration event instead of
 * individual cart domain events, respecting module boundaries.
 */
@Component
public class CartChangeEventConsumer {

  private static final Logger logger = LoggerFactory.getLogger(CartChangeEventConsumer.class);

  private final SyncCheckoutWithCartInputPort syncCheckoutWithCartUseCase;

  public CartChangeEventConsumer(final SyncCheckoutWithCartInputPort syncCheckoutWithCartUseCase) {
    this.syncCheckoutWithCartUseCase = syncCheckoutWithCartUseCase;
  }

  /**
   * Handles CartContentsChangedEvent by syncing the checkout session.
   *
   * <p>When cart contents change (item added, removed, quantity changed), the checkout session's
   * line items are updated to reflect the change. Cart cleared events are logged but do not trigger
   * sync.
   *
   * @param event the cart contents changed integration event
   */
  @ApplicationModuleListener
  public void onCartContentsChanged(final CartContentsChangedEvent event) {
    if (event.changeType() == CartContentsChangedEvent.ChangeType.CART_CLEARED) {
      logger.warn(
          "Cart {} was cleared during checkout - checkout session will have stale data",
          event.cartId());
      return;
    }

    logger.debug(
        "Cart contents changed [{}] for cart {}, syncing checkout session",
        event.changeType(),
        event.cartId());

    syncCheckoutSession(event.cartId().toString());
  }

  private void syncCheckoutSession(final String cartId) {
    try {
      final var response =
          syncCheckoutWithCartUseCase.execute(new SyncCheckoutWithCartCommand(cartId));

      if (response.synced()) {
        logger.info(
            "Checkout session {} synced with cart {} - {} items",
            response.sessionId(),
            cartId,
            response.itemCount());
      }
    } catch (Exception e) {
      logger.error("Failed to sync checkout session with cart {}: {}", cartId, e.getMessage(), e);
    }
  }
}
