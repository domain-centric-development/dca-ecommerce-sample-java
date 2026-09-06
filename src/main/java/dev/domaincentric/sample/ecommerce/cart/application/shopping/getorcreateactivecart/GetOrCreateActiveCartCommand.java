package dev.domaincentric.sample.ecommerce.cart.application.getorcreateactivecart;

/**
 * Command to get or create an active cart for a customer.
 *
 * @param customerId the customer ID
 */
public record GetOrCreateActiveCartCommand(String customerId) {

  public GetOrCreateActiveCartCommand {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
