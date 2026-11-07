package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;

/**
 * What a change-password attempt came to.
 *
 * <p>A rejected password change is a case of the result, not an exception crossing the port. The
 * result says what happened, never in words: the page that shows it words the refusal from the
 * case.
 */
public sealed interface ChangePasswordResult
    permits ChangePasswordResult.Changed,
        ChangePasswordResult.AccountNotAccessible,
        ChangePasswordResult.CurrentPasswordInvalid,
        ChangePasswordResult.NewPasswordRejected {

  /** The password was changed. */
  record Changed() implements ChangePasswordResult {}

  /** No account for the given UserId, or the account cannot log in. */
  record AccountNotAccessible() implements ChangePasswordResult, Refusal {}

  /** The supplied current password does not match the stored one. */
  record CurrentPasswordInvalid() implements ChangePasswordResult, Refusal {}

  /**
   * The new password does not meet the domain's strength rules. Only a strength violation reaches
   * here, never a hasher failure.
   *
   * @param brokenRule the part of the password policy the new password breaks
   */
  record NewPasswordRejected(PasswordRule brokenRule) implements ChangePasswordResult, Refusal {

    public NewPasswordRejected {
      if (brokenRule == null) {
        throw new IllegalArgumentException("A rejected new password names the broken rule");
      }
    }
  }
}
