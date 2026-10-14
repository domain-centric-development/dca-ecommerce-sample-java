package dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart;

/**
 * Query for the customer's active cart, if they have one.
 *
 * @param customerId the customer ID
 */
public record GetActiveCartQuery(String customerId) {

  public GetActiveCartQuery {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
