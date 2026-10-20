package dev.domaincentric.sample.ecommerce.inventory.application.releasestock;

import java.util.List;

/**
 * Command for giving back the stock an order reserved and was then not placed with.
 *
 * @param lines the products and the quantities that were reserved
 */
public record ReleaseStockCommand(List<Line> lines) {

  public ReleaseStockCommand {
    if (lines == null || lines.isEmpty()) {
      throw new IllegalArgumentException("At least one line is required");
    }
    lines = List.copyOf(lines);
  }

  /**
   * One product and the quantity of it that was reserved.
   *
   * @param productId the product ID (UUID string)
   * @param quantity the quantity to release
   */
  public record Line(String productId, int quantity) {

    public Line {
      if (productId == null || productId.isBlank()) {
        throw new IllegalArgumentException("Product ID cannot be null or blank");
      }
      if (quantity <= 0) {
        throw new IllegalArgumentException("Quantity must be positive");
      }
    }
  }
}
