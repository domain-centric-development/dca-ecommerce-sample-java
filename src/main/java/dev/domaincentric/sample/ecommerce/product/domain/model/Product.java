package dev.domaincentric.sample.ecommerce.product.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.sample.ecommerce.product.domain.event.ProductCategoryChanged;
import dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated;
import dev.domaincentric.sample.ecommerce.product.domain.event.ProductDescriptionChanged;
import dev.domaincentric.sample.ecommerce.product.domain.event.ProductNameChanged;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;

/**
 * Product Aggregate Root.
 *
 * <p>Represents a product in the e-commerce catalog with its attributes and business logic. This is
 * the root of the Product aggregate, and all modifications to the product must go through this
 * aggregate root to maintain invariants.
 *
 * <p>Product context owns identity (productId, sku) and description (name, description, category).
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>SKU must be unique across all products
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link dev.domaincentric.sample.ecommerce.product.domain.event.ProductCreated} - when a new
 *       product is created
 *   <li>{@link ProductNameChanged} - when the product name is updated
 *   <li>{@link ProductDescriptionChanged} - when the product description is updated
 *   <li>{@link ProductCategoryChanged} - when the product category is updated
 * </ul>
 *
 * <p><b>Note:</b> Pricing is managed by the Pricing bounded context. Use PricingService to get
 * current prices for products. Stock/availability is managed by the Inventory bounded context. Use
 * InventoryService to get stock information.
 */
public final class Product extends BaseAggregateRoot<Product, ProductId> {

  private final ProductId id;
  private final SKU sku;
  private ProductName name;
  private ProductDescription description;
  private Category category;
  private ImageUrl imageUrl;

  public Product(
      final ProductId id,
      final SKU sku,
      final ProductName name,
      final ProductDescription description,
      final Category category,
      final ImageUrl imageUrl) {
    this.id = id;
    this.sku = sku;
    this.name = name;
    this.description = description;
    this.category = category;
    this.imageUrl = imageUrl;
  }

  /** Creates the aggregate and raises its creation fact; factories delegate here. */
  public static Product create(
      SKU sku,
      ProductName name,
      ProductDescription description,
      Category category,
      ImageUrl imageUrl,
      Price initialPrice,
      int initialStock) {
    if (initialStock < 0) throw new IllegalArgumentException("Initial stock cannot be negative");
    if (initialPrice == null) throw new IllegalArgumentException("Initial price is required");
    Product product = new Product(ProductId.generate(), sku, name, description, category, imageUrl);
    product.registerEvent(ProductCreated.now(product.id(), sku, name, initialPrice, initialStock));
    return product;
  }

  @Override
  public ProductId id() {
    return id;
  }

  public SKU sku() {
    return sku;
  }

  public ProductName name() {
    return name;
  }

  public ProductDescription description() {
    return description;
  }

  public Category category() {
    return category;
  }

  public ImageUrl imageUrl() {
    return imageUrl;
  }

  /**
   * Updates the product name.
   *
   * @param newName the new name
   */
  public void updateName(final ProductName newName) {
    if (newName == null) {
      throw new IllegalArgumentException("New name cannot be null");
    }
    final ProductName oldName = this.name;
    this.name = newName;
    registerEvent(ProductNameChanged.now(this.id, oldName, newName));
  }

  /**
   * Updates the product description.
   *
   * @param newDescription the new description
   */
  public void updateDescription(final ProductDescription newDescription) {
    if (newDescription == null) {
      throw new IllegalArgumentException("New description cannot be null");
    }
    final ProductDescription oldDescription = this.description;
    this.description = newDescription;
    registerEvent(ProductDescriptionChanged.now(this.id, oldDescription, newDescription));
  }

  /**
   * Updates the product category.
   *
   * @param newCategory the new category
   */
  public void updateCategory(final Category newCategory) {
    if (newCategory == null) {
      throw new IllegalArgumentException("New category cannot be null");
    }
    final Category oldCategory = this.category;
    this.category = newCategory;
    registerEvent(ProductCategoryChanged.now(this.id, oldCategory, newCategory));
  }
}
