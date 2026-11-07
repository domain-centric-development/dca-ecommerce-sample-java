package dev.domaincentric.sample.ecommerce.account.application.authenticateaccount;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.account.domain.model.AccountStatus;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import java.util.Set;

/**
 * What authenticating an account came to: the account signed in, the credentials were refused, or
 * the account may not sign in. The result says what happened, never in words: the adapter that
 * shows it words the refusal from the case.
 */
public sealed interface AuthenticateAccountResult
    permits AuthenticateAccountResult.Authenticated,
        AuthenticateAccountResult.CredentialsRefused,
        AuthenticateAccountResult.AccountNotAccessible {

  /**
   * The account signed in; its login time is recorded.
   *
   * @param userId the user's ID (for JWT generation)
   * @param email the user's email
   * @param roles the user's roles
   */
  record Authenticated(String userId, String email, Set<Role> roles)
      implements AuthenticateAccountResult {}

  /**
   * The credentials do not name an account: an address that is no address, an address no account
   * holds, or a wrong password — told apart on purpose by nothing, so the answer does not reveal
   * which addresses are registered.
   */
  record CredentialsRefused() implements AuthenticateAccountResult, Refusal {}

  /**
   * The credentials are right, but the account's status does not allow signing in.
   *
   * @param status the account's status
   */
  record AccountNotAccessible(AccountStatus status) implements AuthenticateAccountResult, Refusal {}
}
