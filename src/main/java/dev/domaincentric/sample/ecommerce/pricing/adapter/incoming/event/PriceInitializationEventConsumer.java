package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.event;

import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceCommand;
import dev.domaincentric.sample.ecommerce.pricing.application.setproductprice.SetProductPriceInputPort;
import dev.domaincentric.sample.ecommerce.pricing.events.PriceInitializationTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Event consumer that creates the price record when a product is created.
 *
 * <p>Uses the Interface Inversion pattern: this consumer listens to {@link
 * PriceInitializationTrigger}, which is defined in the Pricing module's {@code events} package. The
 * producing module (Product Catalog) implements this interface on its {@code ProductCreatedEvent}.
 * This avoids a dependency from Pricing to Product.
 *
 * <p>Idempotent: the use case updates an existing price instead of failing, so a redelivery is
 * harmless.
 */
@Component
public class PriceInitializationEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(PriceInitializationEventConsumer.class);

  private final SetProductPriceInputPort setProductPriceInputPort;

  public PriceInitializationEventConsumer(final SetProductPriceInputPort setProductPriceInputPort) {
    this.setProductPriceInputPort = setProductPriceInputPort;
  }

  /**
   * Creates the initial price for a newly created product.
   *
   * @param event the trigger carrying the product and its initial price
   */
  @ApplicationModuleListener
  void on(final PriceInitializationTrigger event) {
    log.info("Initializing price for product {}", event.productId());

    setProductPriceInputPort.execute(
        new SetProductPriceCommand(
            event.productId(), new java.math.BigDecimal(event.amount()), event.currency()));
  }
}
