package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a cart that is already completed would be completed again.
 *
 * <p>Completion is the step that hands the cart's contents to the confirmed order; doing it twice
 * would claim a second order for the same cart.
 */
public final class CartAlreadyCompletedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public CartAlreadyCompletedException(final CartId cartId) {
    super("Cart " + cartId.value() + " is already completed");
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
