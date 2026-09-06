package dev.domaincentric.sample.ecommerce.checkout.application.getconfirmedcheckoutsession;

/**
 * Query to get a confirmed or completed checkout session for a customer.
 *
 * @param customerId the customer ID
 */
public record GetConfirmedCheckoutSessionQuery(String customerId) {

  public GetConfirmedCheckoutSessionQuery {
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }

  public static GetConfirmedCheckoutSessionQuery of(final String customerId) {
    return new GetConfirmedCheckoutSessionQuery(customerId);
  }
}
