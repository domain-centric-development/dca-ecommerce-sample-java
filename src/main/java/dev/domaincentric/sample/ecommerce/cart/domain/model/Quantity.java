package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;

/** Value Object representing a quantity of items. */
public record Quantity(int value) implements Value {

  public Quantity {
    if (value <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }

  public static Quantity of(final int value) {
    return new Quantity(value);
  }

  /**
   * The quantity of both together.
   *
   * @param other the quantity to add
   * @return the sum
   */
  public Quantity add(final Quantity other) {
    return new Quantity(this.value + other.value);
  }

  /**
   * The quantity left after taking {@code other} away; at least one item must remain.
   *
   * @param other the quantity to take away
   * @return what is left
   * @throws IllegalArgumentException if nothing or less would be left
   */
  public Quantity subtract(final Quantity other) {
    if (this.value - other.value <= 0) {
      throw new IllegalArgumentException("Resulting quantity must be greater than zero");
    }
    return new Quantity(this.value - other.value);
  }

  public Quantity increase() {
    return new Quantity(this.value + 1);
  }

  public Quantity decrease() {
    if (this.value <= 1) {
      throw new IllegalArgumentException("Cannot decrease quantity below 1");
    }
    return new Quantity(this.value - 1);
  }
}
