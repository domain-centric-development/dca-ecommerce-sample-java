package dev.domaincentric.sample.ecommerce.product.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;

/** Value Object representing a Product's image URL. */
public record ImageUrl(String value) implements Value {

  /** The most characters a product image URL holds. */
  public static final int MAX_LENGTH = 2000;

  public ImageUrl {
    if (value == null) {
      throw new IllegalArgumentException("Image URL cannot be null");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("Image URL cannot exceed " + MAX_LENGTH + " characters");
    }
  }

  public static ImageUrl of(final String value) {
    return new ImageUrl(value);
  }

  public static ImageUrl empty() {
    return new ImageUrl("");
  }
}
