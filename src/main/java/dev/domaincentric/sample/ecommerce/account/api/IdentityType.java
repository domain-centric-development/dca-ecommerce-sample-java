package dev.domaincentric.sample.ecommerce.account.api;

/**
 * Whether the caller has authenticated in the current session.
 *
 * <p>A visitor starts as {@link #ANONYMOUS} on the first request and becomes {@link #REGISTERED}
 * once a session names an existing account. The {@code UserId} stays the same across that change,
 * which is what keeps the cart.
 */
public enum IdentityType {
  /**
   * A visitor with an identity but no session — the normal state of a shopper who has not logged
   * in.
   */
  ANONYMOUS,
  /** A visitor whose session names an existing account. */
  REGISTERED;

  public boolean isAnonymous() {
    return this == ANONYMOUS;
  }

  public boolean isRegistered() {
    return this == REGISTERED;
  }
}
