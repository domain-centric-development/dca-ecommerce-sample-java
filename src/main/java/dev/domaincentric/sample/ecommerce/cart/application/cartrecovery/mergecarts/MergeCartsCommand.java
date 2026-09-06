package dev.domaincentric.sample.ecommerce.cart.application.mergecarts;

/**
 * Command to merge carts according to the user's chosen strategy.
 *
 * <p>This command executes the selected cart merge strategy when a user logs in and has items in
 * both their anonymous cart and account cart.
 *
 * @param anonymousUserId the user ID from the anonymous session
 * @param registeredUserId the user ID of the registered account
 * @param strategy the merge strategy chosen by the user
 */
public record MergeCartsCommand(
    String anonymousUserId, String registeredUserId, CartMergeStrategy strategy) {

  public MergeCartsCommand {
    if (anonymousUserId == null || anonymousUserId.isBlank()) {
      throw new IllegalArgumentException("Anonymous user ID cannot be null or blank");
    }
    if (registeredUserId == null || registeredUserId.isBlank()) {
      throw new IllegalArgumentException("Registered user ID cannot be null or blank");
    }
    if (strategy == null) {
      throw new IllegalArgumentException("Strategy cannot be null");
    }
  }
}
