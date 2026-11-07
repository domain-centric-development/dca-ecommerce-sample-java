package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import java.time.LocalDate;

/**
 * What a profile change attempt came to.
 *
 * <p>A rejected profile change is a case of the result, not an exception crossing the port. The
 * result says what happened and which value was refused, never in words: the page that shows it
 * words the refusal from the case.
 */
public sealed interface ChangeProfileResult
    permits ChangeProfileResult.Changed,
        ChangeProfileResult.AccountNotAccessible,
        ChangeProfileResult.EmailAlreadyInUse,
        ChangeProfileResult.EmailInvalid,
        ChangeProfileResult.DateOfBirthInvalid {

  /**
   * The profile was stored.
   *
   * @param profile the stored profile after the change
   */
  record Changed(Profile profile) implements ChangeProfileResult {

    public Changed {
      if (profile == null) {
        throw new IllegalArgumentException("A stored change carries the profile");
      }
    }
  }

  /** No account for the given UserId, or the account cannot log in. */
  record AccountNotAccessible() implements ChangeProfileResult, Refusal {}

  /** Another account already uses the submitted email address. */
  record EmailAlreadyInUse() implements ChangeProfileResult, Refusal {}

  /** The submitted email address is not a valid address. */
  record EmailInvalid() implements ChangeProfileResult, Refusal {}

  /** The submitted date of birth is missing or does not lie before today. */
  record DateOfBirthInvalid() implements ChangeProfileResult, Refusal {}

  /**
   * The stored profile after a successful change.
   *
   * @param email the stored email address
   * @param dateOfBirth the stored date of birth of the account's owner
   */
  record Profile(String email, LocalDate dateOfBirth) {}
}
