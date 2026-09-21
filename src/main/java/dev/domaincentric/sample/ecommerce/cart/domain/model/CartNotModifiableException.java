package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a cart that is no longer active would be changed.
 *
 * <p>Only an active cart takes items, quantity changes or a merge. Once it is completed or
 * abandoned its contents are the record of what happened, and changing them would rewrite history.
 */
public final class CartNotModifiableException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;
  private final CartStatus status;

  public CartNotModifiableException(final CartId cartId, final CartStatus status) {
    super("Cannot modify cart " + cartId.value() + " with status: " + status);
    this.cartId = cartId;
    this.status = status;
  }

  public CartId cartId() {
    return cartId;
  }

  /** The status that refuses the change. */
  public CartStatus status() {
    return status;
  }
}
