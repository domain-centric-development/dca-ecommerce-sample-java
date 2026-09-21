package dev.domaincentric.sample.ecommerce.inventory.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when stock would be decreased by more than the available quantity.
 *
 * <p>The rule is the aggregate's: available quantity never goes negative. Asking for more than
 * there is is a legitimate request with a business answer, not a malformed call — a negative amount
 * would be the malformed one, and that stays an argument guard.
 */
public final class InsufficientStockException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;
  private final int requested;
  private final int available;

  public InsufficientStockException(
      final ProductId productId, final int requested, final int available) {
    super(
        "Cannot decrease stock of "
            + productId.value()
            + " by "
            + requested
            + ", only "
            + available
            + " available");
    this.productId = productId;
    this.requested = requested;
    this.available = available;
  }

  public ProductId productId() {
    return productId;
  }

  public int requested() {
    return requested;
  }

  public int available() {
    return available;
  }
}
