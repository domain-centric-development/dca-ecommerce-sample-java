package dev.domaincentric.sample.ecommerce.cart.application.removeitemfromcart;

/**
 * Command to remove an item from one customer's shopping cart.
 *
 * @param cartId the ID of the cart
 * @param customerId the customer the caller is acting as
 * @param productId the ID of the product to remove
 */
public record RemoveItemFromCartCommand(String cartId, String customerId, String productId) {

  public RemoveItemFromCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
  }
}
