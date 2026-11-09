package dev.domaincentric.sample.ecommerce.inventory.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.sample.ecommerce.inventory.domain.event.StockChanged;
import dev.domaincentric.sample.ecommerce.inventory.domain.event.StockDecreased;
import dev.domaincentric.sample.ecommerce.inventory.domain.event.StockLevelCreated;
import dev.domaincentric.sample.ecommerce.inventory.domain.event.StockReleased;
import dev.domaincentric.sample.ecommerce.inventory.domain.event.StockReserved;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;

/**
 * StockLevel Aggregate Root.
 *
 * <p>Manages inventory stock levels for a product, tracking both available and reserved quantities.
 * Reserved stock represents inventory that has been earmarked for pending orders but not yet
 * shipped.
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>Available quantity cannot be negative
 *   <li>Reserved quantity cannot exceed available quantity
 *   <li>Stock can only be decreased if sufficient quantity exists
 *   <li>Stock can only be reserved if sufficient unreserved quantity exists
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link StockLevelCreated} - when a new stock level is created
 *   <li>{@link StockDecreased} - when stock is decreased
 *   <li>{@link StockReserved} - when stock is reserved
 *   <li>{@link StockReleased} - when reserved stock is released
 * </ul>
 */
public final class StockLevel extends BaseAggregateRoot<StockLevel, StockLevelId> {

  private final StockLevelId id;
  private final ProductId productId;
  private StockQuantity availableQuantity;
  private StockQuantity reservedQuantity;

  private StockLevel(
      final StockLevelId id,
      final ProductId productId,
      final StockQuantity availableQuantity,
      final StockQuantity reservedQuantity) {
    this.id = id;
    this.productId = productId;
    this.availableQuantity = availableQuantity;
    this.reservedQuantity = reservedQuantity;
  }

  /**
   * Creates a new StockLevel for a product with an initial quantity.
   *
   * @param productId the product this stock level is for
   * @param initialQuantity the initial available quantity
   * @return a new StockLevel instance
   */
  public static StockLevel create(final ProductId productId, final int initialQuantity) {
    if (productId == null) {
      throw new IllegalArgumentException("ProductId cannot be null");
    }

    final StockLevel stockLevel =
        new StockLevel(
            StockLevelId.generate(),
            productId,
            StockQuantity.of(initialQuantity),
            StockQuantity.of(0));

    stockLevel.registerEvent(
        StockLevelCreated.now(stockLevel.id, productId, stockLevel.availableQuantity));

    return stockLevel;
  }

  /**
   * Restores a stored stock level as it was saved, without raising a domain event: nothing
   * happened, it was only read back.
   *
   * @param id the stock level's id
   * @param productId the product it counts
   * @param availableQuantity the units on hand
   * @param reservedQuantity the units of those earmarked for pending orders
   * @return the reconstituted stock level, with no pending domain events
   */
  public static StockLevel reconstitute(
      final StockLevelId id,
      final ProductId productId,
      final StockQuantity availableQuantity,
      final StockQuantity reservedQuantity) {
    return new StockLevel(id, productId, availableQuantity, reservedQuantity);
  }

  @Override
  public StockLevelId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public StockQuantity availableQuantity() {
    return availableQuantity;
  }

  public StockQuantity reservedQuantity() {
    return reservedQuantity;
  }

  /**
   * Decreases the available stock quantity.
   *
   * <p>Use this when shipping stock out of inventory.
   *
   * @param amount the amount to subtract from available stock
   * @throws IllegalArgumentException if amount is negative
   * @throws InsufficientStockException if amount exceeds the available quantity
   */
  public void decreaseStock(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amount > this.availableQuantity.value()) {
      throw new InsufficientStockException(this.productId, amount, this.availableQuantity.value());
    }

    final StockQuantity removedQuantity = StockQuantity.of(amount);
    this.availableQuantity = StockQuantity.of(this.availableQuantity.value() - amount);

    // If reserved quantity now exceeds available, adjust it
    if (this.reservedQuantity.value() > this.availableQuantity.value()) {
      this.reservedQuantity = this.availableQuantity;
    }

    registerEvent(
        StockDecreased.now(this.id, this.productId, removedQuantity, this.availableQuantity));
  }

  /**
   * Reserves stock for a pending order.
   *
   * <p>Reserved stock remains in available quantity but is earmarked and cannot be reserved again.
   *
   * @param amount the amount to reserve
   * @throws IllegalArgumentException if amount is negative
   * @throws InsufficientUnreservedStockException if amount exceeds the unreserved stock
   */
  public void reserve(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }

    final int unreserved = this.availableQuantity.value() - this.reservedQuantity.value();
    if (amount > unreserved) {
      throw new InsufficientUnreservedStockException(this.productId, amount, unreserved);
    }

    final StockQuantity reservedAmount = StockQuantity.of(amount);
    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() + amount);

    registerEvent(StockReserved.now(this.id, this.productId, reservedAmount));
  }

  /**
   * Releases previously reserved stock.
   *
   * <p>Use this when an order is cancelled and reserved stock should be made available again.
   *
   * @param amount the amount to release from reservation
   * @throws IllegalArgumentException if amount is negative
   * @throws InsufficientReservedStockException if amount exceeds the reserved quantity
   */
  public void release(final int amount) {
    if (amount < 0) {
      throw new IllegalArgumentException("Amount cannot be negative");
    }
    if (amount > this.reservedQuantity.value()) {
      throw new InsufficientReservedStockException(
          this.productId, amount, this.reservedQuantity.value());
    }

    final StockQuantity releasedQuantity = StockQuantity.of(amount);
    this.reservedQuantity = StockQuantity.of(this.reservedQuantity.value() - amount);

    registerEvent(StockReleased.now(this.id, this.productId, releasedQuantity));
  }

  /**
   * Adjusts the available stock to a counted quantity.
   *
   * <p>Use this for inventory reconciliation or manual stock level adjustments. If the new quantity
   * is lower than the reserved quantity, reserved stock is adjusted accordingly.
   *
   * @param quantity the new available quantity
   * @throws IllegalArgumentException if quantity is negative
   */
  public void adjustStockTo(final int quantity) {
    if (quantity < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative");
    }

    final StockQuantity previousAvailableQuantity = this.availableQuantity;
    final StockQuantity previousReservedQuantity = this.reservedQuantity;

    this.availableQuantity = StockQuantity.of(quantity);

    // If reserved quantity now exceeds available, adjust it
    if (this.reservedQuantity.value() > this.availableQuantity.value()) {
      this.reservedQuantity = this.availableQuantity;
    }

    registerEvent(
        StockChanged.now(
            this.id,
            this.productId,
            previousAvailableQuantity,
            this.availableQuantity,
            previousReservedQuantity,
            this.reservedQuantity));
  }

  /**
   * Checks if there is any unreserved stock available.
   *
   * @return true if available quantity exceeds reserved quantity
   */
  public boolean isAvailable() {
    return this.availableQuantity.value() > this.reservedQuantity.value();
  }
}
