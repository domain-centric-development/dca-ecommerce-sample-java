package dev.domaincentric.sample.ecommerce.inventory.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when more stock would be reserved than is unreserved.
 *
 * <p>Unreserved quantity is available minus reserved — what can still be promised. The aggregate's
 * invariant is that reservations never exceed the available quantity, so a reservation beyond the
 * unreserved part is refused.
 */
public final class InsufficientUnreservedStockException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;
  private final int requested;
  private final int unreserved;

  public InsufficientUnreservedStockException(
      final ProductId productId, final int requested, final int unreserved) {
    super(
        "Cannot reserve "
            + requested
            + " of "
            + productId.value()
            + ", only "
            + unreserved
            + " unreserved");
    this.productId = productId;
    this.requested = requested;
    this.unreserved = unreserved;
  }

  public ProductId productId() {
    return productId;
  }

  public int requested() {
    return requested;
  }

  public int unreserved() {
    return unreserved;
  }
}
