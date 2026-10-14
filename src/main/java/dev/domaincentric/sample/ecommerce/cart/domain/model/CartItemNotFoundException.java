package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Raised when a position the caller names is not in the cart.
 *
 * <p>The cart is asked to change something it does not hold — by position identity or by product.
 * That is a statement about this cart's contents, which only the cart can make, so it is a rule of
 * the model rather than a failed lookup in a store.
 */
public final class CartItemNotFoundException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final @Nullable CartItemId itemId;
  private final @Nullable ProductId productId;

  private CartItemNotFoundException(
      final String message,
      final @Nullable CartItemId itemId,
      final @Nullable ProductId productId) {
    super(message);
    this.itemId = itemId;
    this.productId = productId;
  }

  public static CartItemNotFoundException forItem(final CartItemId itemId) {
    return new CartItemNotFoundException("Cart item not found: " + itemId.value(), itemId, null);
  }

  public static CartItemNotFoundException forProduct(final ProductId productId) {
    return new CartItemNotFoundException(
        "Product not found in cart: " + productId.value(), null, productId);
  }

  /** The cart item that was named, when the caller named an item. */
  public Optional<CartItemId> itemId() {
    return Optional.ofNullable(itemId);
  }

  /** The product that was named, when the caller named a product. */
  public Optional<ProductId> productId() {
    return Optional.ofNullable(productId);
  }

  /** The id the caller named, item or product, as its value. */
  public String reference() {
    return itemId().map(CartItemId::value).orElseGet(() -> productId().orElseThrow().value());
  }
}
