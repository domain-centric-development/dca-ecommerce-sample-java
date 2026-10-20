package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import java.util.List;

/**
 * Command for reserving the stock of every line of an order that is being placed.
 *
 * @param lines the products and the quantities to reserve
 */
public record ReserveStockCommand(List<Line> lines) {

  public ReserveStockCommand {
    if (lines == null || lines.isEmpty()) {
      throw new IllegalArgumentException("At least one line is required");
    }
    lines = List.copyOf(lines);
  }

  /**
   * One product and the quantity to reserve of it.
   *
   * @param productId the product ID (UUID string)
   * @param quantity the quantity to reserve
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
