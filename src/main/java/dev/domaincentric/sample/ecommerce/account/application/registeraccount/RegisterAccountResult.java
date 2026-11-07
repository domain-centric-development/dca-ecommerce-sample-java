package dev.domaincentric.sample.ecommerce.account.application.registeraccount;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.account.domain.model.Email;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Role;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Set;

/**
 * What registering an account came to: the account was registered, or one of the refusals a caller
 * answers. Every refusal is decided before anything is written.
 */
public sealed interface RegisterAccountResult
    permits RegisterAccountResult.Registered,
        RegisterAccountResult.EmailAlreadyRegistered,
        RegisterAccountResult.AccountAlreadyExists,
        RegisterAccountResult.PasswordTooWeak {

  /**
   * The account was registered and linked to the caller's user id.
   *
   * @param accountId the new account's ID
   * @param userId the user's ID (to be used in JWT)
   * @param email the registered email
   * @param roles the user's roles
   */
  record Registered(String accountId, String userId, String email, Set<Role> roles)
      implements RegisterAccountResult {}

  /**
   * Another account already holds that email address. Uniqueness across all accounts is nothing a
   * single account can check, so the rule belongs to the use case and its store, not to the model.
   *
   * @param email the address the request named
   */
  record EmailAlreadyRegistered(Email email) implements RegisterAccountResult, Refusal {}

  /**
   * The signed-in user already has an account and would register a second one — one account per
   * user, checked where the identity of the caller is known.
   *
   * @param userId the caller's user id
   */
  record AccountAlreadyExists(UserId userId) implements RegisterAccountResult, Refusal {}

  /**
   * The password does not meet the strength rules of the model.
   *
   * @param brokenRule the part of the password policy the password breaks
   */
  record PasswordTooWeak(PasswordRule brokenRule) implements RegisterAccountResult, Refusal {}
}
