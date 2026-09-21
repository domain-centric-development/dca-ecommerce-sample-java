package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when a position the caller names is not in the cart.
 *
 * <p>The cart is asked to change something it does not hold — by position identity or by product.
 * That is a statement about this cart's contents, which only the cart can make, so it is a rule of
 * the model rather than a failed lookup in a store.
 */
public final class CartItemNotFoundException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final String reference;

  private CartItemNotFoundException(final String message, final String reference) {
    super(message);
    this.reference = reference;
  }

  /** The position was named by its own identity. */
  public static CartItemNotFoundException forItem(final CartItemId itemId) {
    return new CartItemNotFoundException("Cart item not found: " + itemId.value(), itemId.value());
  }

  /** The position was named by the product it holds. */
  public static CartItemNotFoundException forProduct(final ProductId productId) {
    return new CartItemNotFoundException(
        "Product not found in cart: " + productId.value(), productId.value());
  }

  /** The identity the caller used — a position identity or a product identity. */
  public String reference() {
    return reference;
  }
}
