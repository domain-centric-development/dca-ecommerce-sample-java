package dev.domaincentric.sample.ecommerce.account.application.registeraccount;

import java.util.Set;

/**
 * Response from registering an account.
 *
 * @param accountId the new account's ID
 * @param userId the user's ID (to be used in JWT)
 * @param email the registered email
 * @param roles the codes of the user's roles ({@code Role#code()})
 */
public record RegisterAccountResult(
    String accountId, String userId, String email, Set<String> roles) {

  /**
   * Creates a successful registration response.
   *
   * @param accountId the account ID
   * @param userId the user ID
   * @param email the email
   * @param roles the roles
   * @return a new response
   */
  public static RegisterAccountResult of(
      final String accountId, final String userId, final String email, final Set<String> roles) {
    return new RegisterAccountResult(accountId, userId, email, roles);
  }
}
