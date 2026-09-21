package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import java.io.Serial;

/**
 * Raised when a checkout would start from a cart that is no longer active.
 *
 * <p>A completed or abandoned cart has had its decision; starting a checkout from it would buy the
 * contents of a cart the customer has already left.
 */
public final class CartNotActiveException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public CartNotActiveException(final CartId cartId) {
    super("Cart is not active: " + cartId.value());
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
