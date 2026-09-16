package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.cart.domain.event.CartItemAddedToCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for Shopping Cart domain events.
 *
 * <p>Demonstrates handling domain events after the owning transaction committed — the hook where a
 * context reacts to its own facts (analytics, abandonment timers) without coupling the aggregate to
 * those concerns.
 *
 * <p><b>Transactional Event Handling:</b>
 *
 * <p>Uses {@code @TransactionalEventListener} to ensure events are only handled after the
 * transaction commits successfully. This guarantees that the cart state changes were persisted
 * before triggering downstream operations.
 *
 * <p>This is the DDD pattern of using events to coordinate between aggregates and bounded contexts
 * while maintaining loose coupling (Vaughn Vernon's Rule #4: Use eventual consistency outside the
 * boundary).
 */
@Component
public class CartEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(CartEventConsumer.class);

  /**
   * Handles CartItemAddedToCart events after transaction commit.
   *
   * <p>This handler only executes after the transaction commits successfully, ensuring the cart
   * item was actually added before processing the event.
   *
   * <p>This is where you might:
   *
   * <ul>
   *   <li>Update cart abandonment tracking
   *   <li>Trigger product recommendation engine
   *   <li>Track analytics (which products are added together)
   *   <li>Update customer's recent activity
   * </ul>
   *
   * @param event the cart item added event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCartItemAdded(final CartItemAddedToCart event) {
    log.info(
        "Item added to cart: Product {} (Quantity: {}) added to Cart {} at {}",
        event.productId().value(),
        event.quantity().value(),
        event.cartId().value(),
        event.occurredOn());

    // Example: Track analytics
    // analyticsService.trackCartItemAdded(event);

    // Example: Update recommendations
    // recommendationService.updateBasedOnCartItem(event.customerId(), event.productId());

    // Example: Reset cart abandonment timer
    // cartAbandonmentService.resetTimer(event.cartId());
  }
}
