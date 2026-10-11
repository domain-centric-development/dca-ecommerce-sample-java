package dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.ProcessedEvents;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for setting or updating stock levels.
 *
 * <p>This use case orchestrates stock level management by:
 *
 * <ol>
 *   <li>Looking up existing stock level for the product
 *   <li>Creating a new stock level or updating the existing one
 *   <li>Persisting the changes
 *   <li>Publishing domain events
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link SetStockLevelInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class SetStockLevelUseCase implements SetStockLevelInputPort {

  /** The effect's key in {@link ProcessedEvents} — stable: a rename would replay old events. */
  private static final String EFFECT = "set-stock-level";

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;
  private final ProcessedEvents processedEvents;

  public SetStockLevelUseCase(
      final StockLevelRepository stockLevelRepository,
      final DomainEventPublisher eventPublisher,
      final ProcessedEvents processedEvents) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
    this.processedEvents = processedEvents;
  }

  @Override
  public SetStockLevelResult execute(final SetStockLevelCommand command) {
    final ProductId productId = ProductId.of(command.productId());

    // Check if stock level exists for this product
    final var existingStockLevel = stockLevelRepository.findByProductId(productId);
    final UUID eventId = command.eventId();

    if (eventId != null
        && existingStockLevel.isPresent()
        && processedEvents.contains(EFFECT, eventId)) {
      return toResult(existingStockLevel.get(), false, true);
    }

    final StockLevel stockLevel;
    final boolean created;

    if (existingStockLevel.isPresent()) {
      // Update existing stock level
      stockLevel = existingStockLevel.get();
      stockLevel.adjustStockTo(command.quantity());
      created = false;
    } else {
      // Create new stock level
      stockLevel = StockLevel.create(productId, command.quantity());
      created = true;
    }

    // Persist
    stockLevelRepository.save(stockLevel);

    // Publish domain events
    eventPublisher.publishAndClearEvents(stockLevel);
    if (eventId != null) {
      processedEvents.markProcessed(EFFECT, eventId);
    }

    return toResult(stockLevel, created, false);
  }

  private static SetStockLevelResult toResult(
      final StockLevel stockLevel, final boolean created, final boolean alreadyApplied) {
    return new SetStockLevelResult(
        stockLevel.id().value(),
        stockLevel.productId().value(),
        stockLevel.availableQuantity().value(),
        stockLevel.reservedQuantity().value(),
        created,
        alreadyApplied);
  }
}
