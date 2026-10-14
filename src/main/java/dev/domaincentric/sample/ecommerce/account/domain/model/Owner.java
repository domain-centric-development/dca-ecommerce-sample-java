package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.account.domain.specification.UsableDateOfBirth;
import java.time.LocalDate;

/**
 * Value Object representing the natural person an account belongs to.
 *
 * <p>The name identifies who the account belongs to and is therefore fixed for the lifetime of the
 * account: it is captured once at registration and no operation replaces it. The only detail that
 * may be corrected later is the date of birth, via {@link #withDateOfBirth(LocalDate)} — which
 * carries both names over unchanged, so the immutability of the name is a property of the type
 * rather than a rule a caller has to remember.
 *
 * <p>Names are trimmed but never otherwise normalized: capitalization, particles and spelling of a
 * person's own name are not the account's to correct.
 */
public record Owner(String firstName, String lastName, LocalDate dateOfBirth) implements Value {

  /** Long enough for legal names, short enough to reject a pasted document. */
  private static final int MAX_NAME_LENGTH = 100;

  public Owner {
    firstName = requireName(firstName, "First name");
    lastName = requireName(lastName, "Last name");
    UsableDateOfBirth.RULE.requireSatisfiedBy(dateOfBirth);
  }

  /**
   * Creates an owner.
   *
   * @param firstName the first name as the person writes it
   * @param lastName the last name as the person writes it
   * @param dateOfBirth the date of birth, not in the future
   * @return the owner
   * @throws IllegalArgumentException if a name is blank or too long, or the date is missing or lies
   *     in the future
   */
  public static Owner of(
      final String firstName, final String lastName, final LocalDate dateOfBirth) {
    return new Owner(firstName, lastName, dateOfBirth);
  }

  /**
   * Returns a copy with a corrected date of birth and both names carried over unchanged.
   *
   * @param newDateOfBirth the corrected date of birth, not in the future
   * @return the corrected owner
   * @throws IllegalArgumentException if the date is missing or lies in the future
   */
  public Owner withDateOfBirth(final LocalDate newDateOfBirth) {
    return new Owner(firstName, lastName, newDateOfBirth);
  }

  /**
   * The owner's full name: the first and the last name joined by one space.
   *
   * @return the full name
   */
  public String fullName() {
    return firstName + " " + lastName;
  }

  private static String requireName(final String value, final String label) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(label + " is required");
    }
    final String trimmed = value.trim();
    if (trimmed.length() > MAX_NAME_LENGTH) {
      throw new IllegalArgumentException(label + " is too long");
    }
    return trimmed;
  }
}
