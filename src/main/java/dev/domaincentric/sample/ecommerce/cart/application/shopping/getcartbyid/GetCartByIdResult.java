package dev.domaincentric.sample.ecommerce.cart.application.getcartbyid;

import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import java.util.Optional;

/**
 * Output model for cart retrieval by ID.
 *
 * <p>Wraps the {@link EnrichedCart} read model in an Optional. Use {@link #found()} to check if the
 * cart exists, or use {@link #cart()} directly with Optional methods.
 *
 * @param cart the enriched cart read model wrapped in Optional
 */
public record GetCartByIdResult(Optional<EnrichedCart> cart) {

  public GetCartByIdResult {
    if (cart == null) {
      throw new IllegalArgumentException("Cart optional cannot be null, use Optional.empty()");
    }
  }

  /**
   * Checks if the cart was found.
   *
   * @return true if the cart exists
   */
  public boolean found() {
    return cart.isPresent();
  }

  /** Creates a result for a cart that was not found. */
  public static GetCartByIdResult notFound() {
    return new GetCartByIdResult(Optional.empty());
  }

  /** Creates a result for a cart that was found. */
  public static GetCartByIdResult found(final EnrichedCart cart) {
    return new GetCartByIdResult(Optional.of(cart));
  }
}
