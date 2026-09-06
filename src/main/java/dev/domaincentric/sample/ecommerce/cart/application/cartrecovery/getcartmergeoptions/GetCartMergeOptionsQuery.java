package dev.domaincentric.sample.ecommerce.cart.application.getcartmergeoptions;

/**
 * Query to check if cart merge options should be presented to the user.
 *
 * <p>This query checks whether both the anonymous user and the registered user have active carts
 * with items, which would require the user to choose how to handle the cart conflict.
 *
 * @param anonymousUserId the user ID from the anonymous session (before login)
 * @param registeredUserId the user ID of the registered account (after login)
 */
public record GetCartMergeOptionsQuery(String anonymousUserId, String registeredUserId) {

  public GetCartMergeOptionsQuery {
    if (anonymousUserId == null || anonymousUserId.isBlank()) {
      throw new IllegalArgumentException("Anonymous user ID cannot be null or blank");
    }
    if (registeredUserId == null || registeredUserId.isBlank()) {
      throw new IllegalArgumentException("Registered user ID cannot be null or blank");
    }
  }
}
