package dev.domaincentric.sample.ecommerce.cart.application.createcart;

/**
 * Input model for creating a shopping cart.
 *
 * @param customerId the customer ID who owns the cart
 */
public record CreateCartCommand(String customerId) {

  /** Compact constructor with validation. */
  public CreateCartCommand {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
