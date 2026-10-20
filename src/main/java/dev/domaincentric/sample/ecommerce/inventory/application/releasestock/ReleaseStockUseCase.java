package dev.domaincentric.sample.ecommerce.inventory.application.releasestock;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Use case for giving back the stock an order reserved and was then not placed with.
 *
 * <p>Each line releases what it reserved, up to what is still reserved for its product: a
 * reservation a stocktake capped in the meantime is not released twice.
 */
@Service
public class ReleaseStockUseCase implements ReleaseStockInputPort {

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public ReleaseStockUseCase(
      final StockLevelRepository stockLevelRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public ReleaseStockResult execute(final ReleaseStockCommand command) {
    return stockLevelRepository.inStockLevels(
        () -> transactionBoundary.inTransaction(() -> releaseAll(command.lines())));
  }

  private ReleaseStockResult releaseAll(final List<ReleaseStockCommand.Line> lines) {
    int releasedUnits = 0;
    for (final ReleaseStockCommand.Line line : lines) {
      releasedUnits += release(line);
    }
    return new ReleaseStockResult(releasedUnits);
  }

  private int release(final ReleaseStockCommand.Line line) {
    final Optional<StockLevel> found =
        stockLevelRepository.findByProductId(ProductId.of(line.productId()));
    if (found.isEmpty()) {
      return 0;
    }
    final StockLevel stockLevel = found.get();
    final int released = Math.min(line.quantity(), stockLevel.reservedQuantity().value());
    if (released == 0) {
      return 0;
    }
    stockLevel.release(released);
    stockLevelRepository.save(stockLevel);
    eventPublisher.publishAndClearEvents(stockLevel);
    return released;
  }
}
