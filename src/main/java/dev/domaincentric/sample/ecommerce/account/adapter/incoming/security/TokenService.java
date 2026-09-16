package dev.domaincentric.sample.ecommerce.account.adapter.incoming.security;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Set;

/**
 * Mints the token an authenticated session is carried in.
 *
 * <p>This is adapter-internal mechanics, not a port: a token is a property of the protocol the
 * incoming adapters speak (a cookie for the browser, a Bearer header for API clients), and no use
 * case knows or needs one. The interface therefore lives with its callers in the adapter layer and
 * carries no {@code OutputPort} marker. The JWT implementation is {@code JwtTokenService} in the
 * security adapter.
 *
 * <p>Callers: {@code LoginPageController}, {@code RegisterPageController}, {@code
 * ProfilePageController} and {@code AuthResource}, after a use case has authenticated or registered
 * the account.
 *
 * @see IdentitySession for writing the session cookie
 * @see dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider for
 *     reading the identity — that one is an output port, because the caller's identity is something
 *     the application needs from outside
 */
public interface TokenService {

  /**
   * Generates a token for a registered user.
   *
   * @param userId the user's unique identifier
   * @param email the user's email address
   * @param roles the user's roles (e.g., "CUSTOMER")
   * @return the generated token string
   */
  String generateRegisteredToken(UserId userId, String email, Set<String> roles);
}
