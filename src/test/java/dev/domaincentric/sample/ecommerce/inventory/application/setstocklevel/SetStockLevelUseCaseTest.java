package dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryProcessedEvents;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryStockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** An event sets a stock level once: a redelivery does not undo what happened since. */
class SetStockLevelUseCaseTest {

  private final InMemoryStockLevelRepository stockLevels = new InMemoryStockLevelRepository();
  private final SetStockLevelUseCase useCase =
      new SetStockLevelUseCase(
          stockLevels, new SilentEventPublisher(), new InMemoryProcessedEvents());

  @Test
  @DisplayName("The same event delivered twice sets the stock level once")
  void aRedeliverySetsOnce() {
    final ProductId product = ProductId.generate();
    final UUID eventId = UUID.randomUUID();

    useCase.execute(new SetStockLevelCommand(product.value(), 10, eventId));
    final StockLevel stockLevel = stockLevels.findByProductId(product).orElseThrow();
    stockLevel.adjustStockTo(7);
    stockLevels.save(stockLevel);
    final SetStockLevelResult second =
        useCase.execute(new SetStockLevelCommand(product.value(), 10, eventId));

    assertTrue(second.alreadyApplied());
    assertEquals(7, stockLevels.findByProductId(product).orElseThrow().availableQuantity().value());
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
