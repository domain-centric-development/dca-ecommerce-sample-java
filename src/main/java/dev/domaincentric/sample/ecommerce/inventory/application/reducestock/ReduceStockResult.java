package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import java.util.List;

/**
 * Result of reducing the stock of one sale's lines.
 *
 * @param lines the outcome of each line, in the order of the command; empty when the reduction had
 *     already happened
 * @param alreadyReduced whether the event had already reduced this stock, so nothing changed
 */
public record ReduceStockResult(List<LineOutcome> lines, boolean alreadyReduced) {

  public ReduceStockResult {
    lines = List.copyOf(lines);
  }

  public static ReduceStockResult reduced(final List<LineOutcome> lines) {
    return new ReduceStockResult(lines, false);
  }

  public static ReduceStockResult redelivered() {
    return new ReduceStockResult(List.of(), true);
  }

  /** Whether every line was reduced. */
  public boolean success() {
    return lines.stream().allMatch(LineOutcome::success);
  }

  /**
   * The outcome of one line.
   *
   * @param productId the product ID
   * @param previousStock the stock level before reduction
   * @param newStock the stock level after reduction
   * @param success whether the reduction was successful
   * @param errorMessage error message if reduction failed (null if successful)
   */
  public record LineOutcome(
      String productId, int previousStock, int newStock, boolean success, String errorMessage) {

    public static LineOutcome success(String productId, int previousStock, int newStock) {
      return new LineOutcome(productId, previousStock, newStock, true, null);
    }

    public static LineOutcome failure(String productId, String errorMessage) {
      return new LineOutcome(productId, 0, 0, false, errorMessage);
    }
  }
}
