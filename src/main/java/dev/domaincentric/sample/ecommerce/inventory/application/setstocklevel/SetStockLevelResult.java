package dev.domaincentric.sample.ecommerce.inventory.application.setstocklevel;

/**
 * Output model for setting or updating stock levels.
 *
 * @param stockLevelId the unique identifier of the stock level record
 * @param productId the product ID
 * @param availableQuantity the current available quantity
 * @param reservedQuantity the current reserved quantity
 * @param created true if a new stock level was created, false if an existing one was updated
 * @param alreadyApplied whether the event had set this stock level before, so nothing changed
 */
public record SetStockLevelResult(
    String stockLevelId,
    String productId,
    int availableQuantity,
    int reservedQuantity,
    boolean created,
    boolean alreadyApplied) {}
