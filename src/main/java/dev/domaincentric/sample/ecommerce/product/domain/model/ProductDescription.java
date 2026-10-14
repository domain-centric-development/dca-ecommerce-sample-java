package dev.domaincentric.sample.ecommerce.product.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;

/** Value Object representing a Product's description. */
public record ProductDescription(String value) implements Value {

  /** The most characters a product description holds. */
  public static final int MAX_LENGTH = 2000;

  public ProductDescription {
    if (value == null) {
      throw new IllegalArgumentException("Product description cannot be null");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Product description cannot exceed " + MAX_LENGTH + " characters");
    }
  }

  public static ProductDescription of(final String value) {
    return new ProductDescription(value);
  }

  public static ProductDescription empty() {
    return new ProductDescription("");
  }
}
