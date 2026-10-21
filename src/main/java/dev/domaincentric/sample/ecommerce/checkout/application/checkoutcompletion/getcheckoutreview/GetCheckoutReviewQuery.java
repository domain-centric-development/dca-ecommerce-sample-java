package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getcheckoutreview;

/**
 * Query model for the review of a checkout at today's prices.
 *
 * @param sessionId the checkout session ID to review
 * @param customerId the customer the caller is acting as
 */
public record GetCheckoutReviewQuery(String sessionId, String customerId) {

  public GetCheckoutReviewQuery {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
