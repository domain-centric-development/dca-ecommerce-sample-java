package dev.domaincentric.sample.ecommerce.account.adapter.incoming.security;

/**
 * Establishes and ends the authenticated session of the current browser.
 *
 * <p>This is adapter-internal mechanics, not a port: setting and clearing cookies belongs to the
 * incoming adapter that owns the HTTP protocol, and no use case depends on it. The interface
 * therefore lives with its callers in the adapter layer and carries no {@code OutputPort} marker.
 * The cookie-writing implementation is {@code JwtIdentitySession} in the security adapter; it is
 * request-scoped because it needs the current HTTP response.
 *
 * <p>Only the Account context modifies identity sessions (during login, registration and logout).
 * Other contexts read the resulting identity through {@code IdentityProvider}, which <em>is</em> an
 * output port.
 *
 * @see TokenService for generating tokens
 * @see dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider for
 *     reading identity
 */
public interface IdentitySession {

  /**
   * Sets the session cookie for a registered user and aligns the identity cookie with the account
   * the session belongs to.
   *
   * <p>The cookie settings (HttpOnly, Secure, SameSite, expiration) are managed by the
   * implementation.
   *
   * @param token the authentication token to store
   */
  void setRegisteredIdentity(String token);

  /**
   * Ends the authenticated session because the user asked to leave.
   *
   * <p>Distinct from a session that merely expired: expiry keeps the identity, because nobody
   * decided anything and the identity carries the cart. An explicit logout also gives the browser a
   * <b>new</b> identity, so the next person on a shared device starts clean.
   *
   * <p>Nothing is deleted by this: a registered user's cart belongs to their account and is
   * restored at the next login. See ADR-029 and ADR-030.
   */
  void logOut();
}
