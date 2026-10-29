package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import org.jspecify.annotations.Nullable;

/**
 * Command to recover a guest cart by moving its items into registered user's cart.
 *
 * <p>When a registered user logs in from a new device/browser, this command facilitates merging any
 * items they added as an anonymous user into their existing registered cart. Only the cart of the
 * guest the caller was before the login is taken over: a command naming another guest is refused.
 *
 * @param anonymousUserId the user ID of the anonymous session (before login)
 * @param registeredUserId the user ID of the registered user (after login)
 * @param guestBeforeLogin the guest the caller was before the login, absent where none is known
 */
public record RecoverCartCommand(
    String anonymousUserId, String registeredUserId, @Nullable String guestBeforeLogin) {

  public RecoverCartCommand {
    if (anonymousUserId == null || anonymousUserId.isBlank()) {
      throw new IllegalArgumentException("Anonymous user ID cannot be null or blank");
    }
    if (registeredUserId == null || registeredUserId.isBlank()) {
      throw new IllegalArgumentException("Registered user ID cannot be null or blank");
    }
  }
}
