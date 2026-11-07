package dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;

/** What reading one customer's cart came to: the cart, or no cart of theirs under that identity. */
public sealed interface GetCartByIdResult
    permits GetCartByIdResult.Found, GetCartByIdResult.CartNotFound {

  /**
   * The cart was found.
   *
   * <p>Carries the {@link EnrichedCart} read model and the one figure set the cart page shows that
   * the read model does not carry itself: the totals, with the tax contained in the current
   * subtotal. The use case computes them with the domain's calculator; the adapter only formats
   * them.
   *
   * @param cart the enriched cart read model
   * @param totals the cart's amounts as assembled by the use case
   */
  record Found(EnrichedCart cart, CartTotals totals) implements GetCartByIdResult {}

  /**
   * No cart of this customer under that identity — it does not exist or is somebody else's, told
   * apart on purpose by nothing (ADR-036).
   *
   * @param cartId the cart the caller named
   */
  record CartNotFound(CartId cartId) implements GetCartByIdResult, Refusal {}
}
