package dev.domaincentric.sample.ecommerce.account.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;

/**
 * Port for managing user identity sessions.
 *
 * <p>This is an output port specific to the account bounded context. It abstracts session
 * management (setting/clearing cookies) from the infrastructure layer. Adapters use this port to
 * establish or terminate user sessions without depending on specific session implementations.
 *
 * <p><b>Why in Account Context:</b> Only the account context modifies identity sessions (during
 * login/registration/logout). Other contexts use {@code IdentityProvider} to read the current
 * identity, which was already established by the infrastructure layer.
 *
 * <p><b>Usage in Adapters:</b>
 *
 * <pre>{@code
 * @RestController
 * public class AuthResource {
 *     private final TokenService tokenService;
 *     private final IdentitySession identitySession;
 *
 *     public ResponseEntity<LoginResult> login(...) {
 *         String token = tokenService.generateRegisteredToken(userId, email, roles);
 *         identitySession.setRegisteredIdentity(token);
 *         // ...
 *     }
 *
 *     public ResponseEntity<Void> logout() {
 *         identitySession.logOut();
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p><b>Implementation:</b> The infrastructure layer provides the concrete implementation (e.g.,
 * JwtIdentitySession) that handles cookie management using the current HTTP response.
 *
 * <p><b>Request Scope:</b> Implementations are typically request-scoped because they need access to
 * the current HTTP response to set cookies.
 *
 * @see TokenService for generating tokens
 * @see dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider for
 *     reading identity
 */
public interface IdentitySession extends OutputPort {

  /**
   * Sets the identity cookie for a registered user.
   *
   * <p>This establishes the user's session by storing the token in an HTTP-only cookie. The cookie
   * settings (HttpOnly, Secure, SameSite, expiration) are managed by the infrastructure
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
