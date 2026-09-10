package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

/**
 * Input model for completing a shopping cart after checkout confirmation.
 *
 * @param cartId the cart ID to complete
 */
public record CompleteCartCommand(
    String cartId, String sessionId, java.util.List<String> purchasedPositions) {

  /** Compact constructor with validation. */
  public CompleteCartCommand {
    purchasedPositions = java.util.List.copyOf(purchasedPositions);
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
