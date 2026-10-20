package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import java.util.Optional;

/**
 * Result of reserving the stock of an order's lines.
 *
 * @param refusedProductId the first product, in line order, whose unreserved stock does not cover
 *     its line — nothing was reserved then; empty when every line was reserved
 */
public record ReserveStockResult(Optional<String> refusedProductId) {

  public static ReserveStockResult reserved() {
    return new ReserveStockResult(Optional.empty());
  }

  public static ReserveStockResult refused(final String productId) {
    return new ReserveStockResult(Optional.of(productId));
  }

  /** Whether every line was reserved. */
  public boolean success() {
    return refusedProductId.isEmpty();
  }
}
