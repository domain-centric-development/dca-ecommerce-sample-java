package dev.domaincentric.sample.ecommerce.account.application.shared;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.OutputPort;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Set;

/**
 * Port for generating authentication tokens.
 *
 * <p>This is an output port specific to the account bounded context. It abstracts token generation
 * from the infrastructure layer. Adapters use this port to generate tokens without depending on
 * specific token implementations (JWT, etc.).
 *
 * <p><b>Why in Account Context:</b> Only the account context generates tokens (during
 * login/registration). Other contexts use {@code IdentityProvider} to read the current identity,
 * which is populated from tokens by the infrastructure layer.
 *
 * <p><b>Usage in Adapters:</b>
 *
 * <pre>{@code
 * @RestController
 * public class AuthResource {
 *     private final TokenService tokenService;
 *
 *     public ResponseEntity<LoginResult> login(...) {
 *         String token = tokenService.generateRegisteredToken(userId, email, roles);
 *         // ...
 *     }
 * }
 * }</pre>
 *
 * <p><b>Implementation:</b> The infrastructure layer provides the concrete implementation (e.g.,
 * JwtTokenService) that handles the actual token generation using the chosen token technology.
 *
 * @see dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider for
 *     reading identity
 * @see IdentitySession for managing identity cookies/sessions
 */
public interface TokenService extends OutputPort {

  /**
   * Generates a token for a registered user.
   *
   * <p>This token contains the user's identity information including their ID, email, and roles.
   * The token can be stored in a cookie or returned to the client.
   *
   * @param userId the user's unique identifier
   * @param email the user's email address
   * @param roles the user's roles (e.g., "CUSTOMER")
   * @return the generated token string
   */
  String generateRegisteredToken(UserId userId, String email, Set<String> roles);
}
