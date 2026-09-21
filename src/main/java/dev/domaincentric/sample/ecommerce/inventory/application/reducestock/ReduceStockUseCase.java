package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.InsufficientStockException;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for reducing stock of a product.
 *
 * <p>This use case is invoked when an order is confirmed to reduce the available stock for the
 * ordered products. It publishes domain events for stock changes.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ReduceStockInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class ReduceStockUseCase implements ReduceStockInputPort {

  private static final Logger logger = LoggerFactory.getLogger(ReduceStockUseCase.class);

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;

  public ReduceStockUseCase(
      final StockLevelRepository stockLevelRepository, final DomainEventPublisher eventPublisher) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public ReduceStockResult execute(final ReduceStockCommand command) {
    final ProductId productId = ProductId.of(command.productId());

    final var stockLevelOpt = stockLevelRepository.findByProductId(productId);

    if (stockLevelOpt.isEmpty()) {
      logger.warn("Stock level not found for product: {}", command.productId());
      return ReduceStockResult.failure(
          command.productId(), "Stock level not found for product: " + command.productId());
    }

    final StockLevel stockLevel = stockLevelOpt.get();
    final int previousStock = stockLevel.availableQuantity().value();

    try {
      stockLevel.decreaseStock(command.quantity());
      stockLevelRepository.save(stockLevel);
      eventPublisher.publishAndClearEvents(stockLevel);

      final int newStock = stockLevel.availableQuantity().value();

      logger.info(
          "Stock reduced for product {}: {} -> {} (reduced by {})",
          command.productId(),
          previousStock,
          newStock,
          command.quantity());

      return ReduceStockResult.success(command.productId(), previousStock, newStock);

    } catch (final InsufficientStockException e) {
      // Only the stock rule becomes a failure result. An argument guard from the aggregate would be
      // a malformed call - a defect here, not an answer for the caller - and keeps travelling.
      logger.warn("Stock too low for product {}: {}", command.productId(), e.getMessage());
      return ReduceStockResult.failure(command.productId(), e.getMessage());
    }
  }
}
