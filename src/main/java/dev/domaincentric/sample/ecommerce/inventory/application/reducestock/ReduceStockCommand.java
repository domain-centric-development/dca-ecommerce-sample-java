package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Command for reducing the stock of every line of one sale.
 *
 * <p>{@code eventId} is the id of the integration event that asked for the reduction; the use case
 * does not reduce the stock a second time for the same id. A caller without an event passes none,
 * and the reduction is not deduplicated.
 *
 * @param lines the products and the quantities to take out of stock
 * @param eventId the id of the integration event that asked for the reduction, if any
 */
public record ReduceStockCommand(List<Line> lines, @Nullable UUID eventId) {

  /** A single-line reduction no integration event asked for — not deduplicated. */
  public ReduceStockCommand(final String productId, final int quantity) {
    this(List.of(new Line(productId, quantity)), null);
  }

  public ReduceStockCommand {
    if (lines == null || lines.isEmpty()) {
      throw new IllegalArgumentException("At least one line is required");
    }
    lines = List.copyOf(lines);
  }

  /**
   * One product and the quantity to take out of its stock.
   *
   * @param productId the product ID (UUID string)
   * @param quantity the quantity to reduce
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
