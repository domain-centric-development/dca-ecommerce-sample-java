package dev.domaincentric.sample.ecommerce.cart.application.checkoutcart;

/**
 * Input model for checking out one customer's shopping cart.
 *
 * @param cartId the cart ID to checkout
 * @param customerId the customer the caller is acting as
 */
public record CheckoutCartCommand(String cartId, String customerId) {

  /** Compact constructor with validation. */
  public CheckoutCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
