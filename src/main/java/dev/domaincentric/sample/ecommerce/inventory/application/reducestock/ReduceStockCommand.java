package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Command for reducing stock of a product.
 *
 * <p>{@code eventId} is the id of the integration event that asked for the effect; the use case
 * does not produce the effect a second time for the same id. A caller without an event passes none,
 * and the effect is not deduplicated.
 *
 * @param productId the product ID (UUID string)
 * @param quantity the quantity to reduce
 * @param eventId the id of the integration event that asked for the reduction, if any
 */
public record ReduceStockCommand(String productId, int quantity, @Nullable UUID eventId) {

  /** A reduction no integration event asked for — not deduplicated. */
  public ReduceStockCommand(final String productId, final int quantity) {
    this(productId, quantity, null);
  }

  public ReduceStockCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
  }
}
