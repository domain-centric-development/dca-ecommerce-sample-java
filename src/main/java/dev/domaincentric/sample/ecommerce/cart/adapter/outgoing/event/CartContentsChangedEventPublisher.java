package dev.domaincentric.sample.ecommerce.cart.adapter.outgoing.event;

import dev.domaincentric.sample.ecommerce.cart.domain.event.CartCleared;
import dev.domaincentric.sample.ecommerce.cart.domain.event.CartItemAddedToCart;
import dev.domaincentric.sample.ecommerce.cart.domain.event.ProductRemovedFromCart;
import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent;
import dev.domaincentric.sample.ecommerce.cart.events.CartContentsChangedEvent.ChangeType;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outgoing event adapter that translates internal cart domain events into a consolidated {@link
 * CartContentsChangedEvent} integration event for cross-context consumption.
 *
 * <p>Listens to the CartItemAddedToCart, ProductRemovedFromCart and CartCleared domain events and
 * publishes a single integration event type that other modules can consume.
 */
@Component
public class CartContentsChangedEventPublisher {

  private static final Logger logger =
      LoggerFactory.getLogger(CartContentsChangedEventPublisher.class);

  private final ApplicationEventPublisher publisher;

  public CartContentsChangedEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @EventListener
  public void onItemAdded(final CartItemAddedToCart event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.ITEM_ADDED);
  }

  @EventListener
  public void onItemRemoved(final ProductRemovedFromCart event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.ITEM_REMOVED);
  }

  @EventListener
  public void onCartCleared(final CartCleared event) {
    publish(UUID.fromString(event.cartId().value()), ChangeType.CART_CLEARED);
  }

  private void publish(UUID cartId, ChangeType changeType) {
    var integrationEvent = CartContentsChangedEvent.now(cartId, changeType);
    logger.debug("Publishing CartContentsChangedEvent [{}] for cart: {}", changeType, cartId);
    publisher.publishEvent(integrationEvent);
  }
}
