package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * Data transfer object representing cart data from the Cart bounded context.
 *
 * <p>This is part of the Anti-Corruption Layer (ACL) that translates Cart context data into the
 * Checkout context's language.
 */
public record CartData(
    CartId cartId, CustomerId customerId, List<CartItemData> items, boolean active) {

  /** Represents a cart item in terms the Checkout context understands. */
  public record CartItemData(
      ProductId productId, Price priceAtAddition, int quantity, String positionSnapshot) {}
}
