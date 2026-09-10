package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import org.jspecify.annotations.Nullable;

/**
 * Value Object representing a line item in the checkout.
 *
 * <p>Contains product information, quantity, and calculated totals for a single item in the
 * checkout session.
 */
public record CheckoutLineItem(
    CheckoutLineItemId id,
    ProductId productId,
    String productName,
    Money unitPrice,
    int quantity,
    @Nullable String imageUrl,
    String positionSnapshot)
    implements Value {

  public CheckoutLineItem(
      CheckoutLineItemId id,
      ProductId productId,
      String productName,
      Money unitPrice,
      int quantity,
      @Nullable String imageUrl) {
    this(id, productId, productName, unitPrice, quantity, imageUrl, "");
  }

  public CheckoutLineItem {
    if (id == null) {
      throw new IllegalArgumentException("Line item ID cannot be null");
    }
    if (productId == null) {
      throw new IllegalArgumentException("Product ID cannot be null");
    }
    if (productName == null || productName.isBlank()) {
      throw new IllegalArgumentException("Product name cannot be null or blank");
    }
    if (unitPrice == null) {
      throw new IllegalArgumentException("Unit price cannot be null");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be greater than zero");
    }
  }

  public static CheckoutLineItem of(
      final CheckoutLineItemId id,
      final ProductId productId,
      final String productName,
      final Money unitPrice,
      final int quantity,
      @Nullable final String imageUrl) {
    return new CheckoutLineItem(id, productId, productName, unitPrice, quantity, imageUrl);
  }

  public Money lineTotal() {
    return unitPrice.multiply(quantity);
  }

  public CheckoutLineItem withQuantity(final int newQuantity) {
    return new CheckoutLineItem(
        id, productId, productName, unitPrice, newQuantity, imageUrl, positionSnapshot);
  }
}
