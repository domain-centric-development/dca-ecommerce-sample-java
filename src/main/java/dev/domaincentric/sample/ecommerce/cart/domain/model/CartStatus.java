package dev.domaincentric.sample.ecommerce.cart.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;

/** Value Object representing the status of a shopping cart. */
public enum CartStatus implements Value {
  /** Cart is active and can be modified. */
  ACTIVE,

  /** Cart checkout has been completed (order confirmed). */
  COMPLETED,

  /** Cart has been abandoned by the customer. */
  ABANDONED
}
