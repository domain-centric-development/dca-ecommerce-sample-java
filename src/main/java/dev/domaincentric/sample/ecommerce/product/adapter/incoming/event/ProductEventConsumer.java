package dev.domaincentric.sample.ecommerce.product.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Logs every created product, after the transaction that created it committed.
 *
 * <p>An after-commit callback that only writes a log line: it calls no port, so it needs no
 * transaction of its own, and a log line lost to a process that stops right after the commit is
 * acceptable. A reaction that changes state or reaches another system does not belong here: it is a
 * transactional listener ({@code @ApplicationModuleListener}) that drives a use case - see the
 * guide's "Reacting to an event". Pricing and Inventory react to the published {@code
 * ProductCreatedEvent} in their own contexts.
 */
@Component
public class ProductEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);

  /**
   * Logs the created product.
   *
   * @param event the product created event
   */
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onProductCreated(final ProductCreated event) {
    log.info(
        "Product created: {} (SKU: {}, Name: {}) at {}",
        event.productId().value(),
        event.sku().value(),
        event.name().value(),
        event.occurredOn());
  }
}
