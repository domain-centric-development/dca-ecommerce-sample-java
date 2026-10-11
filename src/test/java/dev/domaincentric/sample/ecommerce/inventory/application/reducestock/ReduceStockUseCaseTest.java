package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryProcessedEvents;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryStockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelUseCase;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Reducing stock happens once per event and product, however often the event is delivered. */
class ReduceStockUseCaseTest {

  private final InMemoryStockLevelRepository stockLevels = new InMemoryStockLevelRepository();
  private final InMemoryProcessedEvents processedEvents = new InMemoryProcessedEvents();
  private final DomainEventPublisher publisher = new SilentEventPublisher();
  private final ReduceStockUseCase useCase =
      new ReduceStockUseCase(stockLevels, publisher, processedEvents);

  @Test
  @DisplayName("The same event reducing the same product twice reduces its stock once")
  void aRedeliveryReducesOnce() {
    final ProductId product = stocked(50);
    final UUID eventId = UUID.randomUUID();

    useCase.execute(new ReduceStockCommand(product.value(), 3, eventId));
    final ReduceStockResult second =
        useCase.execute(new ReduceStockCommand(product.value(), 3, eventId));

    assertTrue(second.alreadyReduced());
    assertEquals(47, available(product));
  }

  @Test
  @DisplayName("One event reduces each of its products")
  void oneEventReducesEveryProduct() {
    final ProductId first = stocked(50);
    final ProductId second = stocked(20);
    final UUID eventId = UUID.randomUUID();

    useCase.execute(new ReduceStockCommand(first.value(), 3, eventId));
    useCase.execute(new ReduceStockCommand(second.value(), 2, eventId));

    assertEquals(47, available(first));
    assertEquals(18, available(second));
  }

  @Test
  @DisplayName("A reduction no event asked for is not deduplicated")
  void withoutAnEventEveryCallReduces() {
    final ProductId product = stocked(50);

    useCase.execute(new ReduceStockCommand(product.value(), 3));
    useCase.execute(new ReduceStockCommand(product.value(), 3));

    assertEquals(44, available(product));
  }

  @Test
  @DisplayName("Two effects of the context receiving the same event id both run")
  void twoEffectsOfOneEventBothRun() {
    final ProductId product = ProductId.generate();
    final UUID eventId = UUID.randomUUID();
    final SetStockLevelUseCase setStockLevel =
        new SetStockLevelUseCase(stockLevels, publisher, processedEvents);

    setStockLevel.execute(new SetStockLevelCommand(product.value(), 30, eventId));
    useCase.execute(new ReduceStockCommand(product.value(), 5, eventId));

    assertEquals(25, available(product));
  }

  private ProductId stocked(final int quantity) {
    final ProductId product = ProductId.generate();
    stockLevels.save(StockLevel.create(product, quantity));
    return product;
  }

  private int available(final ProductId product) {
    return stockLevels.findByProductId(product).orElseThrow().availableQuantity().value();
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
