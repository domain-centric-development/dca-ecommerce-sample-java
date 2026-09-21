package dev.domaincentric.sample.ecommerce.cart.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import java.io.Serial;

/**
 * Raised when the addressed cart is not available to the asking customer.
 *
 * <p>Deliberately one failure for two situations: the cart does not exist, and the cart belongs to
 * somebody else. Telling those apart would let a stranger probe which cart identities are real, so
 * the lookup asks for the cart <em>of this customer</em> and reports the same thing either way
 * (ADR-036).
 */
public final class CartNotFoundException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public CartNotFoundException(final CartId cartId) {
    super("Cart not found: " + cartId.value());
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
