package dev.domaincentric.sample.ecommerce.inventory.application.reservestock;

import dev.domaincentric.dca.buildingblocks.application.Refusal;

/** What reserving the stock of an order's lines came to — all lines or none. */
public sealed interface ReserveStockResult
    permits ReserveStockResult.Reserved, ReserveStockResult.InsufficientUnreservedStock {

  /** Every line was reserved. */
  record Reserved() implements ReserveStockResult {}

  /**
   * A line's unreserved stock does not cover it — or Inventory holds no stock for its product.
   * Every line was checked before any was reserved, so nothing was reserved.
   *
   * @param productId the first product, in line order, whose line is not covered
   */
  record InsufficientUnreservedStock(String productId) implements ReserveStockResult, Refusal {}
}
