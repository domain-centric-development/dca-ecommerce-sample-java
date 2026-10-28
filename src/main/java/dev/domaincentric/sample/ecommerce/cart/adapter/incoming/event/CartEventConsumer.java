package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.cart.domain.event.CartItemAddedToCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Logs every item added to a cart, after the transaction that added it committed.
 *
 * <p>An after-commit callback that only writes a log line: it calls no port, so it needs no
 * transaction of its own, and a log line lost to a process that stops right after the commit is
 * acceptable. A reaction that changes state or reaches another system does not belong here: it is a
 * transactional listener ({@code @ApplicationModuleListener}) that drives a use case - see the
 * guide's "Reacting to an event".
 */
@Component
public class CartEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(CartEventConsumer.class);

  /**
   * Logs the added item.
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
  }
}
