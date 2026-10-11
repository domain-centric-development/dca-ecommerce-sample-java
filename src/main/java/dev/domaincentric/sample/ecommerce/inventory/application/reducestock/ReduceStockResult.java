package dev.domaincentric.sample.ecommerce.inventory.application.reducestock;

import java.util.List;
import java.util.Optional;

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

  /** Why a line was not reduced. */
  public enum Failure {
    /** The inventory keeps no stock level for the product. */
    STOCK_LEVEL_NOT_FOUND,
    /** Less is in stock than the line asks for; the stock stays as it was. */
    INSUFFICIENT_STOCK
  }

  /**
   * The outcome of one line. A failure is named by its type and values, never in words: whoever
   * reports it words it.
   *
   * @param productId the product ID
   * @param previousStock the stock level before reduction (for a failure, the stock as it is)
   * @param newStock the stock level after reduction (for a failure, the stock as it is)
   * @param failure why the line was not reduced, empty if it was
   */
  public record LineOutcome(
      String productId, int previousStock, int newStock, Optional<Failure> failure) {

    public static LineOutcome success(String productId, int previousStock, int newStock) {
      return new LineOutcome(productId, previousStock, newStock, Optional.empty());
    }

    public static LineOutcome stockLevelNotFound(String productId) {
      return new LineOutcome(productId, 0, 0, Optional.of(Failure.STOCK_LEVEL_NOT_FOUND));
    }

    public static LineOutcome insufficientStock(String productId, int available) {
      return new LineOutcome(
          productId, available, available, Optional.of(Failure.INSUFFICIENT_STOCK));
    }

    /** Whether the line was reduced. */
    public boolean success() {
      return failure.isEmpty();
    }
  }
}
