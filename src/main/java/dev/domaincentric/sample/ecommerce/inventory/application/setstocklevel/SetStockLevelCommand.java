package dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Input model for setting or updating stock levels.
 *
 * <p>{@code eventId} is the id of the integration event that asked for the effect; the use case
 * does not produce the effect a second time for the same id. A caller without an event passes none,
 * and the effect is not deduplicated.
 *
 * @param productId the product ID
 * @param quantity the stock quantity to set (must be non-negative)
 * @param eventId the id of the integration event that asked for it, if any
 */
public record SetStockLevelCommand(String productId, int quantity, @Nullable UUID eventId) {

  /** A stock level no integration event asked for — not deduplicated. */
  public SetStockLevelCommand(final String productId, final int quantity) {
    this(productId, quantity, null);
  }

  /** Compact constructor with validation. */
  public SetStockLevelCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative");
    }
  }
}
