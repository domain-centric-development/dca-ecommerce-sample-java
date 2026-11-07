package dev.domaincentric.sample.ecommerce.account.application.getprofile;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import java.time.LocalDate;

/**
 * What reading the profile came to: the profile, or no accessible account for the identity — a case
 * of the result, not an exception.
 */
public sealed interface GetProfileResult
    permits GetProfileResult.Found, GetProfileResult.AccountNotAccessible {

  /**
   * The profile projection.
   *
   * @param profile the profile fields the page renders
   */
  record Found(Profile profile) implements GetProfileResult {}

  /** No account for the given UserId, or the account cannot log in. */
  record AccountNotAccessible() implements GetProfileResult, Refusal {}

  /**
   * Projection of the profile fields the page renders.
   *
   * <p>The names are projected for display only — they are not editable.
   *
   * @param email the account's email address
   * @param firstName the owner's first name
   * @param lastName the owner's last name
   * @param fullName the owner's full name, first and last name joined by one space
   * @param dateOfBirth the owner's date of birth
   */
  record Profile(
      String email, String firstName, String lastName, String fullName, LocalDate dateOfBirth) {}
}
