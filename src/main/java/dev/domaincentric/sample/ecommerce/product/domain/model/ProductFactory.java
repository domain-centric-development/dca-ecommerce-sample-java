package dev.domaincentric.sample.ecommerce.product.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Factory;
import dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;

/**
 * Factory for creating Product aggregates.
 *
 * <p>Encapsulates complex product creation logic and ensures all invariants are satisfied from the
 * moment of creation.
 *
 * <p><b>Note:</b> Pricing is managed by the Pricing bounded context. The initial price is included
 * in the ProductCreated event for cross-context synchronization. Stock/availability is managed by
 * the Inventory bounded context.
 */
public final class ProductFactory implements Factory {

  /**
   * Creates a new product with generated ID.
   *
   * <p>Raises a {@link ProductCreated} domain event with the initial price for synchronization with
   * the Pricing bounded context (and stock for Inventory context).
   *
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param category the product category
   * @param imageUrl the product image URL
   * @param initialPrice the initial price (for Pricing context synchronization)
   * @param initialStock the initial stock quantity (for Inventory context synchronization)
   * @return a new Product aggregate
   */
  public Product createProduct(
      final SKU sku,
      final ProductName name,
      final ProductDescription description,
      final Category category,
      final ImageUrl imageUrl,
      final Price initialPrice,
      final int initialStock) {

    return Product.create(sku, name, description, category, imageUrl, initialPrice, initialStock);
  }

  /**
   * Creates a new product with specified ID.
   *
   * <p>Note: This method does NOT raise a ProductCreated event. Use this for reconstituting
   * products from persistence or for testing purposes only.
   *
   * @param id the product ID
   * @param sku the SKU
   * @param name the product name
   * @param description the product description
   * @param category the product category
   * @param imageUrl the product image URL
   * @return a new Product aggregate
   */
  public Product createProductWithId(
      final ProductId id,
      final SKU sku,
      final ProductName name,
      final ProductDescription description,
      final Category category,
      final ImageUrl imageUrl) {

    return new Product(id, sku, name, description, category, imageUrl);
  }

  /**
   * Creates a new product with default empty description.
   *
   * @param sku the SKU
   * @param name the product name
   * @param category the product category
   * @param initialPrice the initial price (for Pricing context synchronization)
   * @param initialStock the initial stock quantity (for Inventory context synchronization)
   * @return a new Product aggregate
   */
  public Product createBasicProduct(
      final SKU sku,
      final ProductName name,
      final Category category,
      final Price initialPrice,
      final int initialStock) {

    return createProduct(
        sku,
        name,
        ProductDescription.empty(),
        category,
        ImageUrl.empty(),
        initialPrice,
        initialStock);
  }
}
