package dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession;

/**
 * Query to get an active checkout session for a customer.
 *
 * @param customerId the customer ID
 */
public record GetActiveCheckoutSessionQuery(String customerId) {

  public GetActiveCheckoutSessionQuery {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }

  public static GetActiveCheckoutSessionQuery of(final String customerId) {
    return new GetActiveCheckoutSessionQuery(customerId);
  }
}
