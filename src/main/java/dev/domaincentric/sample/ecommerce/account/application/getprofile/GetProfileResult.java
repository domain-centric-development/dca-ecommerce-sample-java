package dev.domaincentric.sample.ecommerce.account.application.getprofile;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Output model of the Get Profile use case.
 *
 * <p>An identity without an accessible account is a normal return value, not an exception
 * (ADR-023).
 *
 * @param profile the profile projection, empty if no accessible account exists
 */
public record GetProfileResult(Optional<Profile> profile) {

  public boolean found() {
    return profile.isPresent();
  }

  public static GetProfileResult found(final Profile profile) {
    return new GetProfileResult(Optional.of(profile));
  }

  public static GetProfileResult notFound() {
    return new GetProfileResult(Optional.empty());
  }

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
  public record Profile(
      String email, String firstName, String lastName, String fullName, LocalDate dateOfBirth) {}
}
