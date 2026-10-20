package dev.domaincentric.sample.ecommerce.inventory.application.releasestock;

/**
 * Result of giving back reserved stock.
 *
 * @param releasedUnits how many units were released over all lines — never more than was reserved
 */
public record ReleaseStockResult(int releasedUnits) {}
