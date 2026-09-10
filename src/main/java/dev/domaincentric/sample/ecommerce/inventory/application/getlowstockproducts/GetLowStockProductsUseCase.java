package dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts;

import dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts.GetLowStockProductsResult.LowStockProduct;
import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockQuantity;
import dev.domaincentric.sample.ecommerce.inventory.domain.specification.AvailableQuantityBelow;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for the low stock overview: which products have fallen below a threshold the operator
 * gives, so that they can be reordered before they run out.
 *
 * <p>The quantity reported is the available quantity as Inventory defines it — the stock on hand,
 * regardless of whether parts of it are already reserved.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetLowStockProductsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetLowStockProductsUseCase implements GetLowStockProductsInputPort {

  private final StockLevelRepository stockLevelRepository;

  public GetLowStockProductsUseCase(final StockLevelRepository stockLevelRepository) {
    this.stockLevelRepository = stockLevelRepository;
  }

  @Override
  public GetLowStockProductsResult execute(final GetLowStockProductsQuery query) {
    final List<StockLevel> runningLow =
        stockLevelRepository.findBy(
            new AvailableQuantityBelow(StockQuantity.of(query.threshold())));

    return new GetLowStockProductsResult(runningLow.stream().map(this::mapToLowStock).toList());
  }

  private LowStockProduct mapToLowStock(final StockLevel stockLevel) {
    return new LowStockProduct(stockLevel.productId(), stockLevel.availableQuantity().value());
  }
}
