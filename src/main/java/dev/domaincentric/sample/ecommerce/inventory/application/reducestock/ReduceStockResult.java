package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

/**
 * Result of reducing stock for a product.
 *
 * @param productId the product ID
 * @param previousStock the stock level before reduction
 * @param newStock the stock level after reduction
 * @param success whether the reduction was successful
 * @param errorMessage error message if reduction failed (null if successful)
 * @param alreadyReduced whether the event had already reduced this product's stock, so nothing
 *     changed
 */
public record ReduceStockResult(
    String productId,
    int previousStock,
    int newStock,
    boolean success,
    String errorMessage,
    boolean alreadyReduced) {

  public static ReduceStockResult success(String productId, int previousStock, int newStock) {
    return new ReduceStockResult(productId, previousStock, newStock, true, null, false);
  }

  public static ReduceStockResult alreadyReduced(String productId, int currentStock) {
    return new ReduceStockResult(productId, currentStock, currentStock, true, null, true);
  }

  public static ReduceStockResult failure(String productId, String errorMessage) {
    return new ReduceStockResult(productId, 0, 0, false, errorMessage, false);
  }
}
