package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.inventory.application.reducestock.ReduceStockResult.LineOutcome;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.ProcessedEvents;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.InsufficientStockException;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Use case for reducing the stock of every line of one sale.
 *
 * <p>This use case is invoked when an order is confirmed to reduce the available stock for the
 * ordered products, consuming the reservation the order made when it was placed. It runs inside the
 * stock levels' gate, so it never interleaves with a reservation, and publishes domain events for
 * stock changes.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link ReduceStockInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class ReduceStockUseCase implements ReduceStockInputPort {

  private static final Logger logger = LoggerFactory.getLogger(ReduceStockUseCase.class);

  /** The effect's key in {@link ProcessedEvents} — stable: a rename would replay old events. */
  private static final String EFFECT = "reduce-stock";

  private final StockLevelRepository stockLevelRepository;
  private final DomainEventPublisher eventPublisher;
  private final ProcessedEvents processedEvents;
  private final TransactionBoundary transactionBoundary;

  public ReduceStockUseCase(
      final StockLevelRepository stockLevelRepository,
      final DomainEventPublisher eventPublisher,
      final ProcessedEvents processedEvents,
      final TransactionBoundary transactionBoundary) {
    this.stockLevelRepository = stockLevelRepository;
    this.eventPublisher = eventPublisher;
    this.processedEvents = processedEvents;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public ReduceStockResult execute(final ReduceStockCommand command) {
    return stockLevelRepository.inStockLevels(
        () -> transactionBoundary.inTransaction(() -> reduceAll(command)));
  }

  private ReduceStockResult reduceAll(final ReduceStockCommand command) {
    final UUID eventId = command.eventId();
    if (eventId != null && processedEvents.contains(EFFECT, eventId)) {
      logger.info("Stock already reduced for event {}", eventId);
      return ReduceStockResult.redelivered();
    }

    // A line that cannot be reduced does not stop the others: the sale has happened.
    final List<LineOutcome> outcomes = new ArrayList<>();
    for (final ReduceStockCommand.Line line : command.lines()) {
      outcomes.add(reduce(line));
    }

    if (eventId != null) {
      processedEvents.markProcessed(EFFECT, eventId);
    }
    return ReduceStockResult.reduced(outcomes);
  }

  /**
   * The sale consumes the reservation its order made when it was placed: what the line reserved is
   * given back before the reduction, so it is not held a second time beside it. A line the stock
   * cannot cover releases nothing — its stock stays as it was.
   */
  private static void consumeReservation(final StockLevel stockLevel, final int quantity) {
    final int reserved = Math.min(quantity, stockLevel.reservedQuantity().value());
    if (reserved > 0 && quantity <= stockLevel.availableQuantity().value()) {
      stockLevel.release(reserved);
    }
  }

  private LineOutcome reduce(final ReduceStockCommand.Line line) {
    final ProductId productId = ProductId.of(line.productId());

    final var stockLevelOpt = stockLevelRepository.findByProductId(productId);

    if (stockLevelOpt.isEmpty()) {
      logger.warn("Stock level not found for product: {}", line.productId());
      return LineOutcome.stockLevelNotFound(line.productId());
    }

    final StockLevel stockLevel = stockLevelOpt.get();
    final int previousStock = stockLevel.availableQuantity().value();

    try {
      consumeReservation(stockLevel, line.quantity());
      stockLevel.decreaseStock(line.quantity());
      stockLevelRepository.save(stockLevel);
      eventPublisher.publishAndClearEvents(stockLevel);

      final int newStock = stockLevel.availableQuantity().value();

      logger.info(
          "Stock reduced for product {}: {} -> {} (reduced by {})",
          line.productId(),
          previousStock,
          newStock,
          line.quantity());

      return LineOutcome.success(line.productId(), previousStock, newStock);

    } catch (final InsufficientStockException e) {
      // Only the stock rule becomes a failure outcome. An argument guard from the aggregate would
      // be
      // a malformed call - a defect here, not an answer for the caller - and keeps travelling.
      logger.warn("Stock too low for product {}: {}", line.productId(), e.getMessage());
      return LineOutcome.insufficientStock(line.productId(), e.available());
    }
  }
}
