package dev.domaincentric.sample.ecommerce.inventory.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when more stock would be released than is reserved.
 *
 * <p>Releasing returns an earmarked quantity to the promisable pool; releasing more than was ever
 * reserved would invent stock the warehouse does not have.
 */
public final class InsufficientReservedStockException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;
  private final int requested;
  private final int reserved;

  public InsufficientReservedStockException(
      final ProductId productId, final int requested, final int reserved) {
    super(
        "Cannot release "
            + requested
            + " of "
            + productId.value()
            + ", only "
            + reserved
            + " reserved");
    this.productId = productId;
    this.requested = requested;
    this.reserved = reserved;
  }

  public ProductId productId() {
    return productId;
  }

  public int requested() {
    return requested;
  }

  public int reserved() {
    return reserved;
  }
}
