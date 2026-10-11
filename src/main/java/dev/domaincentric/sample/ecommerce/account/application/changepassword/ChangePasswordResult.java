package dev.domaincentric.sample.ecommerce.account.application.changepassword;

import dev.domaincentric.sample.ecommerce.account.domain.model.PasswordRule;
import java.util.Optional;

/**
 * Output model of the Change Password use case.
 *
 * <p>Per ADR-023 a rejected password change is a normal return value, not an exception crossing the
 * port. The result says what happened, never in words: the page that shows it words the refusal
 * from the outcome.
 *
 * @param outcome what happened
 * @param brokenRule the part of the password policy the new password breaks, present exactly for
 *     {@link Outcome#NEW_PASSWORD_REJECTED}
 */
public record ChangePasswordResult(Outcome outcome, Optional<PasswordRule> brokenRule) {

  public ChangePasswordResult {
    if (outcome == null) {
      throw new IllegalArgumentException("Outcome cannot be null");
    }
    if (brokenRule == null) {
      throw new IllegalArgumentException(
          "Broken rule optional cannot be null, use Optional.empty()");
    }
    if ((outcome == Outcome.NEW_PASSWORD_REJECTED) != brokenRule.isPresent()) {
      throw new IllegalArgumentException(
          "A rejected new password names the broken rule, any other outcome does not: " + outcome);
    }
  }

  /** Possible outcomes of a change-password attempt. */
  public enum Outcome {
    /** The password was changed. */
    CHANGED,
    /** No account for the given UserId, or the account cannot log in. */
    ACCOUNT_NOT_ACCESSIBLE,
    /** The supplied current password does not match the stored one. */
    CURRENT_PASSWORD_INVALID,
    /** The new password does not meet the domain's strength rules. */
    NEW_PASSWORD_REJECTED
  }

  public static ChangePasswordResult changed() {
    return new ChangePasswordResult(Outcome.CHANGED, Optional.empty());
  }

  public static ChangePasswordResult accountNotAccessible() {
    return new ChangePasswordResult(Outcome.ACCOUNT_NOT_ACCESSIBLE, Optional.empty());
  }

  public static ChangePasswordResult currentPasswordInvalid() {
    return new ChangePasswordResult(Outcome.CURRENT_PASSWORD_INVALID, Optional.empty());
  }

  /** Only a strength violation reaches here, never a hasher failure. */
  public static ChangePasswordResult newPasswordRejected(final PasswordRule brokenRule) {
    return new ChangePasswordResult(Outcome.NEW_PASSWORD_REJECTED, Optional.of(brokenRule));
  }
}
