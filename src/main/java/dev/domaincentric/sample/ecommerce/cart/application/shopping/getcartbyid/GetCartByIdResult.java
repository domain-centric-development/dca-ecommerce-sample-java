package dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid;

import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Output model for cart retrieval by ID.
 *
 * <p>Wraps the {@link EnrichedCart} read model in an Optional and adds the one figure the cart page
 * shows that the read model does not carry itself: the tax contained in the current subtotal. The
 * use case computes it with the domain's calculator; the adapter only formats it.
 *
 * @param cart the enriched cart read model wrapped in Optional
 * @param totals the cart's amounts as assembled by the use case (null if not found)
 */
public record GetCartByIdResult(Optional<EnrichedCart> cart, @Nullable CartTotals totals) {

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
    return new GetCartByIdResult(Optional.empty(), null);
  }

  /** Creates a result for a cart that was found. */
  public static GetCartByIdResult found(final EnrichedCart cart, final CartTotals totals) {
    return new GetCartByIdResult(Optional.of(cart), totals);
  }
}
