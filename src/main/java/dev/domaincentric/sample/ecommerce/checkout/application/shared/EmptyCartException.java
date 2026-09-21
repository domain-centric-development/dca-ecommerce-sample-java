package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import java.io.Serial;

/**
 * Raised when a checkout would start from a cart that holds nothing.
 *
 * <p>Read before the session exists, so the customer is told to put something in the cart rather
 * than meeting the same rule one step later, where the model states it for the session itself.
 */
public final class EmptyCartException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public EmptyCartException(final CartId cartId) {
    super("Cannot checkout empty cart: " + cartId.value());
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
