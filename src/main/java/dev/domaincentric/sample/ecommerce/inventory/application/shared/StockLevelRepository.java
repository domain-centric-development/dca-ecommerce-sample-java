package dev.domaincentric.sample.ecommerce.inventory.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Repository;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevel;
import dev.domaincentric.sample.ecommerce.inventory.domain.model.StockLevelId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.specification.CompositeSpecification;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for StockLevel aggregate.
 *
 * <p>Provides collection-like access to StockLevel aggregates using domain language. Implementation
 * resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 *
 * <ul>
 *   <li>{@code findById(StockLevelId)} - inherited from base interface
 *   <li>{@code save(StockLevel)} - inherited from base interface
 *   <li>{@code deleteById(StockLevelId)} - inherited from base interface
 * </ul>
 */
public interface StockLevelRepository extends Repository<StockLevel, StockLevelId> {

  /**
   * Finds the stock level for a specific product.
   *
   * @param productId the product ID to search for
   * @return the stock level if found, empty otherwise
   */
  Optional<StockLevel> findByProductId(ProductId productId);

  /**
   * Finds stock levels for multiple products.
   *
   * @param productIds the collection of product IDs to search for
   * @return list of stock levels for the given product IDs
   */
  List<StockLevel> findByProductIds(Collection<ProductId> productIds);

  /**
   * Finds every stock level. Only an overview across the assortment has a reason to ask for this.
   *
   * @return all stock levels
   */
  List<StockLevel> findAll();

  /**
   * Finds the stock levels matching the given specification.
   *
   * <p>The specification is expressed in domain terms and translated by the persistence adapter
   * into native predicates (see {@link CompositeSpecification#accept}), so the filtering happens
   * where the data is.
   *
   * <p>The default implementation filters in memory, so a secondary adapter can opt in to
   * database-side push-down progressively.
   */
  default List<StockLevel> findBy(CompositeSpecification<StockLevel> specification) {
    return findAll().stream().filter(specification::isSatisfiedBy).toList();
  }
}
