package dev.domaincentric.sample.ecommerce.account.application.authenticateaccount;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import java.util.Set;

/**
 * Response from authenticating an account.
 *
 * @param success whether authentication was successful
 * @param userId the user's ID (for JWT generation)
 * @param email the user's email
 * @param roles the user's roles
 * @param errorMessage error message if authentication failed
 */
public record AuthenticateAccountResult(
    boolean success, String userId, String email, Set<Role> roles, String errorMessage) {

  /**
   * Creates a successful authentication response.
   *
   * @param userId the user ID
   * @param email the email
   * @param roles the roles
   * @return a successful response
   */
  public static AuthenticateAccountResult success(
      final String userId, final String email, final Set<Role> roles) {
    return new AuthenticateAccountResult(true, userId, email, roles, null);
  }

  /**
   * Creates a failed authentication response.
   *
   * @param errorMessage the error message
   * @return a failed response
   */
  public static AuthenticateAccountResult failure(final String errorMessage) {
    return new AuthenticateAccountResult(false, null, null, null, errorMessage);
  }
}
