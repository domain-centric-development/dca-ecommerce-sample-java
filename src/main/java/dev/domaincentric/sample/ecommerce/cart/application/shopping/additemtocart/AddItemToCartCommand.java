package dev.domaincentric.sample.ecommerce.cart.application.additemtocart;

/**
 * Input model for adding an item to one customer's shopping cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer the caller is acting as
 * @param productId the product ID to add
 * @param quantity the quantity to add
 */
public record AddItemToCartCommand(
    String cartId, String customerId, String productId, int quantity) {

  /** Compact constructor with validation. */
  public AddItemToCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("Quantity must be positive");
    }
  }
}
