package dev.domaincentric.sample.ecommerce.product.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.Repository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product aggregate.
 *
 * <p>Provides collection-like access to Product aggregates using domain language. Implementation
 * resides in the secondary adapter layer.
 *
 * <p>Extends the base {@link Repository} interface which provides common methods:
 *
 * <ul>
 *   <li>{@code findById(ProductId)} - inherited from base interface
 *   <li>{@code save(Product)} - inherited from base interface
 *   <li>{@code deleteById(ProductId)} - inherited from base interface
 * </ul>
 */
public interface ProductRepository extends Repository<Product, ProductId> {

  /**
   * Finds a product by its SKU (Stock Keeping Unit).
   *
   * @param sku the SKU to search for
   * @return the product if found, empty otherwise
   */
  Optional<Product> findBySku(SKU sku);

  /**
   * Finds all products in a specific category.
   *
   * @param category the category to filter by
   * @return list of products in the category, ordered by product name (ordinal)
   */
  List<Product> findByCategory(Category category);

  /**
   * Retrieves all products.
   *
   * <p>The order is part of the contract: products are sorted by product name (ordinal comparison),
   * so the catalog reads the same in every persistence profile and in the .NET twin.
   *
   * @return list of all products, ordered by product name
   */
  List<Product> findAll();

  /**
   * Checks if a product with the given SKU exists.
   *
   * @param sku the SKU to check
   * @return true if exists, false otherwise
   */
  boolean existsBySku(SKU sku);
}
