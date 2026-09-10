package dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts;

/**
 * Input model for the low stock overview.
 *
 * @param threshold the quantity the available quantity is compared against; a product with exactly
 *     this quantity on hand is not running low (must be non-negative)
 */
public record GetLowStockProductsQuery(int threshold) {

  /** Compact constructor with validation. */
  public GetLowStockProductsQuery {
    if (threshold < 0) {
      throw new IllegalArgumentException("Threshold cannot be negative");
    }
  }
}
