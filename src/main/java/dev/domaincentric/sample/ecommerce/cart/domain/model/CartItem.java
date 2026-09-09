package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Entity;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;

/**
 * CartItem Entity.
 *
 * <p>Represents an item within a shopping cart. This is an entity within the ShoppingCart aggregate
 * and should only be created and modified through the ShoppingCart aggregate root.
 *
 * <p><b>Important:</b> CartItem is NOT an aggregate root. It cannot exist outside of a ShoppingCart
 * and has package-private constructors to enforce this boundary.
 */
public final class CartItem implements Entity<CartItem, CartItemId> {

  private final CartItemId id;
  private final ProductId productId; // Reference to Product by ID only
  private Quantity quantity;
  private PositionUnits units;
  private final Price priceAtAddition; // Price snapshot when item was added

  /** Package-private constructor - CartItems can only be created through ShoppingCart. */
  CartItem(
      final CartItemId id,
      final ProductId productId,
      final Quantity quantity,
      final Price priceAtAddition) {
    this.id = id;
    this.productId = productId;
    this.quantity = quantity;
    this.units = PositionUnits.initial(quantity.value());
    this.priceAtAddition = priceAtAddition;
  }

  public String positionSnapshot() {
    return id.value() + ":" + units.serialize();
  }

  public String storedUnits() {
    return units.serialize();
  }

  void restoreUnits(String encoded) {
    var restored = PositionUnits.parse(encoded);
    if (restored.quantity() != quantity.value())
      throw new IllegalArgumentException("Stored unit count mismatch");
    units = restored;
  }

  boolean reconcile(PositionUnits purchased) {
    var remaining = units.reconcile(purchased);
    if (remaining.quantity() == units.quantity()) return false;
    units = remaining;
    if (units.quantity() > 0) quantity = Quantity.of(units.quantity());
    return true;
  }

  boolean hasUnits() {
    return units.quantity() > 0;
  }

  @Override
  public CartItemId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public Quantity quantity() {
    return quantity;
  }

  public Price priceAtAddition() {
    return priceAtAddition;
  }

  /** Package-private - only ShoppingCart aggregate can modify quantity. */
  void updateQuantity(final Quantity newQuantity) {
    if (newQuantity == null) {
      throw new IllegalArgumentException("Quantity cannot be null");
    }
    this.units = units.resize(newQuantity.value());
    this.quantity = newQuantity;
  }

  /** Package-private - only ShoppingCart aggregate can increase quantity. */
  void increaseQuantity() {
    updateQuantity(this.quantity.increase());
  }

  /** Package-private - only ShoppingCart aggregate can decrease quantity. */
  void decreaseQuantity() {
    updateQuantity(this.quantity.decrease());
  }
}
