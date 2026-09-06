package dev.domaincentric.sample.ecommerce.cart.application.getcartbyid;

/**
 * Input model for retrieving one customer's cart by ID.
 *
 * <p>The cart ID says <i>which</i> cart is meant; the customer ID says <i>whose</i>. Without it the
 * query would be unanswerable without guessing, and every adapter would have to guard it for
 * itself.
 *
 * @param cartId the cart ID
 * @param customerId the customer the caller is acting as
 */
public record GetCartByIdQuery(String cartId, String customerId) {

  /** Compact constructor with validation. */
  public GetCartByIdQuery {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
