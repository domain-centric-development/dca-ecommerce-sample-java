package dev.domaincentric.sample.ecommerce.pricing.application.provisioninitialprice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence.InMemoryProcessedEvents;
import dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence.InMemoryProductPriceRepository;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** An event sets a price once: a redelivery does not undo a price set since. */
class ProvisionInitialPriceUseCaseTest {

  private final InMemoryProductPriceRepository prices = new InMemoryProductPriceRepository();
  private final InMemoryProcessedEvents processedEvents = new InMemoryProcessedEvents();
  private final ProvisionInitialPriceUseCase useCase =
      new ProvisionInitialPriceUseCase(prices, new SilentEventPublisher(), processedEvents);

  @Test
  @DisplayName("The same event delivered twice sets the price once")
  void aRedeliverySetsOnce() {
    final ProductId product = ProductId.generate();
    final UUID eventId = UUID.randomUUID();

    useCase.execute(
        new ProvisionInitialPriceCommand(product.value(), new BigDecimal("9.99"), "EUR", eventId));
    useCase.execute(
        new ProvisionInitialPriceCommand(product.value(), new BigDecimal("12.00"), "EUR"));
    final ProvisionInitialPriceResult redelivery =
        useCase.execute(
            new ProvisionInitialPriceCommand(
                product.value(), new BigDecimal("9.99"), "EUR", eventId));

    assertTrue(redelivery.alreadyApplied());
    assertEquals(new BigDecimal("12.00"), redelivery.priceAmount());
  }

  @Test
  @DisplayName("Two effects receiving the same event id are recorded apart")
  void effectsAreRecordedApart() {
    final UUID eventId = UUID.randomUUID();

    processedEvents.markProcessed("set-product-price", eventId);

    assertTrue(processedEvents.contains("set-product-price", eventId));
    assertEquals(false, processedEvents.contains("another-effect", eventId));
  }

  private static final class SilentEventPublisher implements DomainEventPublisher {
    @Override
    public void publish(final DomainEvent event) {}

    @Override
    public void publishAndClearEvents(final AggregateRoot<?, ?> aggregate) {
      aggregate.clearDomainEvents();
    }
  }
}
