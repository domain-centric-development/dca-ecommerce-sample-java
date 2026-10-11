package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;

/**
 * Raised when a plaintext password does not meet the account's strength rules.
 *
 * <p>A rule of the model, not an argument contract: the caller passed a perfectly well-formed
 * string, and the model refuses it for what it is made of. Its own type is what lets a use case
 * report the refusal to the person choosing the password, while a malformed call from the hashing
 * adapter keeps travelling as the defect it is. It names the broken {@link PasswordRule}; the
 * message is for the log, and whoever shows the refusal words it from the rule.
 */
public final class PasswordTooWeakException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final PasswordRule rule;

  public PasswordTooWeakException(final PasswordRule rule) {
    super(describe(rule));
    this.rule = rule;
  }

  /** The part of the policy the password breaks. */
  public PasswordRule rule() {
    return rule;
  }

  private static String describe(final PasswordRule rule) {
    return switch (rule) {
      case TOO_SHORT ->
          "Password must be at least " + HashedPassword.MIN_LENGTH + " characters long";
      case TOO_LONG ->
          "Password must not be longer than "
              + HashedPassword.MAX_BYTE_LENGTH
              + " bytes (UTF-8 encoded)";
      case NO_UPPERCASE_LETTER -> "Password must contain at least one uppercase letter";
      case NO_LOWERCASE_LETTER -> "Password must contain at least one lowercase letter";
      case NO_DIGIT -> "Password must contain at least one digit";
    };
  }
}
