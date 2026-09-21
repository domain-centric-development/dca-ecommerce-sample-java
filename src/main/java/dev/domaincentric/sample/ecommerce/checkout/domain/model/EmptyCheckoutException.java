package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a checkout would start without a single line item.
 *
 * <p>A checkout is the act of buying what is in the cart; with nothing in it there is nothing to
 * buy, and every later step would compute totals over an empty list.
 */
public final class EmptyCheckoutException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CartId cartId;

  public EmptyCheckoutException(final CartId cartId) {
    super("Cannot start checkout for cart " + cartId.value() + " without line items");
    this.cartId = cartId;
  }

  public CartId cartId() {
    return cartId;
  }
}
