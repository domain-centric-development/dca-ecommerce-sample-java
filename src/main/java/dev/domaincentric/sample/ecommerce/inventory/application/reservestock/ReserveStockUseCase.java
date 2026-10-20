package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for reserving the stock of every line of an order that is being placed.
 *
 * <p>All lines or none: every line is checked against the unreserved stock before any is reserved,
 * and the whole operation runs inside the stock levels' gate, so two orders placed at once never
 * both count on the same units.
 */
@Service
public class ReserveStockUseCase implements ReserveStockInputPort {

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public ReserveStockUseCase(
      final StockLevelRepository stockLevelRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public ReserveStockResult execute(final ReserveStockCommand command) {
    return stockLevelRepository.inStockLevels(
        () -> transactionBoundary.inTransaction(() -> reserveAll(command.lines())));
  }

  private ReserveStockResult reserveAll(final List<ReserveStockCommand.Line> lines) {
    final List<StockLevel> stockLevels = new ArrayList<>();
    for (final ReserveStockCommand.Line line : lines) {
      final Optional<StockLevel> stockLevel =
          stockLevelRepository.findByProductId(ProductId.of(line.productId()));
      if (stockLevel.isEmpty() || unreserved(stockLevel.get()) < line.quantity()) {
        return ReserveStockResult.refused(line.productId());
      }
      stockLevels.add(stockLevel.get());
    }

    for (int i = 0; i < lines.size(); i++) {
      final StockLevel stockLevel = stockLevels.get(i);
      stockLevel.reserve(lines.get(i).quantity());
      stockLevelRepository.save(stockLevel);
      eventPublisher.publishAndClearEvents(stockLevel);
    }
    return ReserveStockResult.reserved();
  }

  /** The quantity that can still be reserved: available minus reserved. */
  private static int unreserved(final StockLevel stockLevel) {
    return stockLevel.availableQuantity().value() - stockLevel.reservedQuantity().value();
  }
}
