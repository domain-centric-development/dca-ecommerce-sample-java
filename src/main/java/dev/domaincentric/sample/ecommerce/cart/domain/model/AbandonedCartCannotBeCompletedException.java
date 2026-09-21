package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when an abandoned cart would be completed.
 *
 * <p>Abandoning is the customer's decision that this cart is over. A later confirmation must start
 * from a cart the customer still holds, not revive the one they left.
 */
public final class AbandonedCartCannotBeCompletedException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public AbandonedCartCannotBeCompletedException(final CartId cartId) {
    super("Cannot complete abandoned cart " + cartId.value());
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
