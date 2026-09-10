package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.event;

import dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated;
import dev.domaincentric.sample.ecommerce.product.events.ProductCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Outgoing event adapter that translates the internal {@link ProductCreated} domain event into a
 * {@link ProductCreatedEvent} integration event for cross-context consumption.
 *
 * <p>Pricing and Inventory contexts consume this event to create initial price entries and stock
 * levels respectively.
 */
@Component
public class ProductCreatedEventPublisher {

  private static final Logger logger = LoggerFactory.getLogger(ProductCreatedEventPublisher.class);

  private final ApplicationEventPublisher publisher;

  public ProductCreatedEventPublisher(final ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  /** Listens for the internal domain event and publishes the integration event. */
  @EventListener
  public void on(final ProductCreated domainEvent) {
    var integrationEvent =
        new ProductCreatedEvent(
            domainEvent.eventId(),
            domainEvent.occurredOn(),
            domainEvent.productId().value(),
            domainEvent.initialPrice().value().amount().toPlainString(),
            domainEvent.initialPrice().value().currency().getCurrencyCode(),
            domainEvent.initialStock());

    logger.info("Publishing ProductCreatedEvent for product: {}", domainEvent.productId().value());

    publisher.publishEvent(integrationEvent);
  }
}
