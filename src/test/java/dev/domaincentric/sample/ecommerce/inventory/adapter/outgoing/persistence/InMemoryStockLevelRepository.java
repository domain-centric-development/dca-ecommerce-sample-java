package dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevelId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fake of {@link StockLevelRepository} for tests without a database: one stock level per
 * product, stored and handed out as copies, so a change survives only through {@code save}.
 */
public class InMemoryStockLevelRepository implements StockLevelRepository {

  private final ConcurrentHashMap<StockLevelId, StockLevel> stockLevels = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, StockLevelId> productIdIndex =
      new ConcurrentHashMap<>();

  @Override
  public Optional<StockLevel> findById(final StockLevelId id) {
    return Optional.ofNullable(stockLevels.get(id)).map(InMemoryStockLevelRepository::copyOf);
  }

  @Override
  public Optional<StockLevel> findByProductId(final ProductId productId) {
    final StockLevelId stockLevelId = productIdIndex.get(productId);
    if (stockLevelId == null) {
      return Optional.empty();
    }
    return findById(stockLevelId);
  }

  @Override
  public List<StockLevel> findByProductIds(final Collection<ProductId> productIds) {
    return productIds.stream()
        .map(productIdIndex::get)
        .filter(stockLevelId -> stockLevelId != null)
        .map(stockLevels::get)
        .filter(stockLevel -> stockLevel != null)
        .map(InMemoryStockLevelRepository::copyOf)
        .toList();
  }

  @Override
  public List<StockLevel> findAll() {
    return stockLevels.values().stream().map(InMemoryStockLevelRepository::copyOf).toList();
  }

  @Override
  public StockLevel save(final StockLevel stockLevel) {
    stockLevels.put(stockLevel.id(), copyOf(stockLevel));
    productIdIndex.put(stockLevel.productId(), stockLevel.id());
    return stockLevel;
  }

  @Override
  public void deleteById(final StockLevelId id) {
    final StockLevel stockLevel = stockLevels.remove(id);
    if (stockLevel != null) {
      productIdIndex.remove(stockLevel.productId());
    }
  }

  private static StockLevel copyOf(final StockLevel stockLevel) {
    return StockLevel.reconstitute(
        stockLevel.id(),
        stockLevel.productId(),
        stockLevel.availableQuantity(),
        stockLevel.reservedQuantity());
  }
}
