package dev.domaincentric.sample.ecommerce.cart.application.recovercart;

/**
 * Command to recover cart on login by merging anonymous cart into registered user's cart.
 *
 * <p>When a registered user logs in from a new device/browser, this command facilitates merging any
 * items they added as an anonymous user into their existing registered cart.
 *
 * @param anonymousUserId the user ID of the anonymous session (before login)
 * @param registeredUserId the user ID of the registered user (after login)
 */
public record RecoverCartOnLoginCommand(String anonymousUserId, String registeredUserId) {

  public RecoverCartOnLoginCommand {
    if (anonymousUserId == null || anonymousUserId.isBlank()) {
      throw new IllegalArgumentException("Anonymous user ID cannot be null or blank");
    }
    if (registeredUserId == null || registeredUserId.isBlank()) {
      throw new IllegalArgumentException("Registered user ID cannot be null or blank");
    }
  }
}
