package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.AggregateRoot;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryProcessedEvents;
import dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence.InMemoryStockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockCommand.Line;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelCommand;
import dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel.SetStockLevelUseCase;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** One sale's stock is reduced once, however often its event is delivered. */
class ReduceStockUseCaseTest {

  private final InMemoryStockLevelRepository stockLevels = new InMemoryStockLevelRepository();
  private final InMemoryProcessedEvents processedEvents = new InMemoryProcessedEvents();
  private final DomainEventPublisher publisher = new SilentEventPublisher();
  private final ReduceStockUseCase useCase =
      new ReduceStockUseCase(stockLevels, publisher, processedEvents);

  @Test
  @DisplayName("The same event delivered twice reduces every line once")
  void aRedeliveryReducesOnce() {
    final ProductId first = stocked(50);
    final ProductId second = stocked(20);
    final ReduceStockCommand sale =
        new ReduceStockCommand(
            List.of(new Line(first.value(), 3), new Line(second.value(), 2)), UUID.randomUUID());

    final ReduceStockResult once = useCase.execute(sale);
    final ReduceStockResult again = useCase.execute(sale);

    assertFalse(once.alreadyReduced());
    assertTrue(again.alreadyReduced());
    assertEquals(47, available(first));
    assertEquals(18, available(second));
  }

  @Test
  @DisplayName("A line that cannot be reduced does not stop the others")
  void aFailingLineDoesNotStopTheOthers() {
    final ProductId scarce = stocked(1);
    final ProductId plenty = stocked(20);

    final ReduceStockResult result =
        useCase.execute(
            new ReduceStockCommand(
                List.of(new Line(scarce.value(), 5), new Line(plenty.value(), 2)),
                UUID.randomUUID()));

    assertFalse(result.lines().get(0).success());
    assertTrue(result.lines().get(1).success());
    assertEquals(18, available(plenty));
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
  @DisplayName("Two effects receiving the same event id both run")
  void twoEffectsOfOneEventBothRun() {
    final ProductId product = ProductId.generate();
    final UUID eventId = UUID.randomUUID();
    final SetStockLevelUseCase setStockLevel =
        new SetStockLevelUseCase(stockLevels, publisher, processedEvents);

    setStockLevel.execute(new SetStockLevelCommand(product.value(), 10, eventId));
    useCase.execute(new ReduceStockCommand(List.of(new Line(product.value(), 2)), eventId));

    assertEquals(8, available(product));
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
