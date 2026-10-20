package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import java.util.Optional;

/**
 * Output port that holds the stock an order needs while it is being placed.
 *
 * <p>Placing an order reserves the stock of every line before the order is confirmed, so that two
 * orders placed at once never both count on the same units. An order that is then not placed gives
 * its reservation back.
 */
public interface StockReservationPort extends OutputPort {

  /**
   * One line of an order: a product and how many of it.
   *
   * @param productId the product
   * @param quantity how many of it, positive
   */
  record Line(ProductId productId, int quantity) {

    public Line {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
    }
  }

  /**
   * Reserves the stock of every line, or of none.
   *
   * @param lines the order's lines
   * @return empty when every line was reserved; otherwise the first product, in line order, whose
   *     stock no longer covers its line — and nothing was reserved
   */
  Optional<ProductId> reserve(List<Line> lines);

  /**
   * Gives back what {@link #reserve(List)} reserved for these lines.
   *
   * @param lines the lines that were reserved
   */
  void release(List<Line> lines);
}
