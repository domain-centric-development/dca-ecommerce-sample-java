package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import java.io.Serial;

/**
 * Raised when the cart a checkout would start from is not available to the asking customer.
 *
 * <p>Checkout does not own the cart; it reads it through its own port. A cart that is missing and a
 * cart that belongs to somebody else are reported alike, for the same reason the session lookup
 * does it (ADR-036).
 */
public final class CartNotAvailableException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public CartNotAvailableException(final CartId cartId) {
    super("Cart not found: " + cartId.value());
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
