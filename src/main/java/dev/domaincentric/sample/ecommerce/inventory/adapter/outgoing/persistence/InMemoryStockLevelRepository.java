package dev.domaincentric.sample.ecommerce.inventory.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.inventory.application.shared.StockLevelRepository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevelId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of StockLevelRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for stock levels using
 * ConcurrentHashMap. A secondary index on ProductId enables efficient lookups by product.
 *
 * <p>Stock levels are created via SampleDataInitializer which coordinates initialization across all
 * bounded contexts (Product, Pricing, Inventory).
 *
 * <p>In a production system, this would be replaced with a database implementation.
 */
@Repository
public class InMemoryStockLevelRepository implements StockLevelRepository {

  private final ConcurrentHashMap<StockLevelId, StockLevel> stockLevels = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, StockLevelId> productIdIndex =
      new ConcurrentHashMap<>();

  @Override
  public Optional<StockLevel> findById(final StockLevelId id) {
    return Optional.ofNullable(stockLevels.get(id));
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
        .toList();
  }

  @Override
  public List<StockLevel> findAll() {
    return List.copyOf(stockLevels.values());
  }

  @Override
  public StockLevel save(final StockLevel stockLevel) {
    stockLevels.put(stockLevel.id(), stockLevel);
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
}
