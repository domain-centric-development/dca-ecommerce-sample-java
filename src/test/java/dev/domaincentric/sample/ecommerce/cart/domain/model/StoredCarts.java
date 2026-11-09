package dev.domaincentric.sample.ecommerce.cart.domain.model;

/** Brings a cart into a status the way a repository restores a stored one. */
final class StoredCarts {

  private StoredCarts() {}

  /**
   * The same cart — id, owner and lines — as it would be read back with another status.
   *
   * @param cart the cart to restore
   * @param status the status it was stored with
   * @return the restored cart, with no pending domain events
   */
  static ShoppingCart withStatus(final ShoppingCart cart, final CartStatus status) {
    return ShoppingCart.reconstitute(
        cart.id(),
        cart.customerId(),
        status,
        cart.items().stream()
            .map(
                item ->
                    new ShoppingCart.StoredItem(
                        item.id(),
                        item.productId(),
                        item.quantity(),
                        item.priceAtAddition(),
                        item.storedUnits()))
            .toList());
  }
}
