package dev.domaincentric.sample.ecommerce.cart.application.completecart;

/**
 * Input model for completing a shopping cart after checkout confirmation.
 *
 * @param cartId the cart ID to complete
 */
public record CompleteCartCommand(String cartId) {

  /** Compact constructor with validation. */
  public CompleteCartCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
