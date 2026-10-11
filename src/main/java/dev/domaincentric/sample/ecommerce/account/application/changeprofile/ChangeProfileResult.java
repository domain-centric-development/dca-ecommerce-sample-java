package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Output model of the Change Profile use case.
 *
 * <p>Per ADR-023 a rejected profile change is a normal return value, not an exception crossing the
 * port. The result says what happened and which value was refused, never in words: the page that
 * shows it words the refusal from the outcome.
 *
 * @param outcome what happened
 * @param profile the stored profile after the change, present exactly for {@link Outcome#CHANGED}
 */
public record ChangeProfileResult(Outcome outcome, Optional<Profile> profile) {

  public ChangeProfileResult {
    if (outcome == null) {
      throw new IllegalArgumentException("Outcome cannot be null");
    }
    if (profile == null) {
      throw new IllegalArgumentException("Profile optional cannot be null, use Optional.empty()");
    }
    if ((outcome == Outcome.CHANGED) != profile.isPresent()) {
      throw new IllegalArgumentException(
          "Only a stored change carries the profile, and it always does: " + outcome);
    }
  }

  /** Possible outcomes of a profile change attempt. */
  public enum Outcome {
    /** The profile was stored. */
    CHANGED,
    /** No account for the given UserId, or the account cannot log in. */
    ACCOUNT_NOT_ACCESSIBLE,
    /** Another account already uses the submitted email address. */
    EMAIL_ALREADY_IN_USE,
    /** The submitted email address is not a valid address. */
    EMAIL_INVALID,
    /** The submitted date of birth is missing or lies in the future. */
    DATE_OF_BIRTH_INVALID
  }

  public static ChangeProfileResult changed(final Profile profile) {
    return new ChangeProfileResult(Outcome.CHANGED, Optional.of(profile));
  }

  public static ChangeProfileResult accountNotAccessible() {
    return new ChangeProfileResult(Outcome.ACCOUNT_NOT_ACCESSIBLE, Optional.empty());
  }

  public static ChangeProfileResult emailAlreadyInUse() {
    return new ChangeProfileResult(Outcome.EMAIL_ALREADY_IN_USE, Optional.empty());
  }

  public static ChangeProfileResult emailInvalid() {
    return new ChangeProfileResult(Outcome.EMAIL_INVALID, Optional.empty());
  }

  public static ChangeProfileResult dateOfBirthInvalid() {
    return new ChangeProfileResult(Outcome.DATE_OF_BIRTH_INVALID, Optional.empty());
  }

  /**
   * The stored profile after a successful change.
   *
   * @param email the stored email address
   * @param dateOfBirth the stored date of birth of the account's owner
   */
  public record Profile(String email, LocalDate dateOfBirth) {}
}
