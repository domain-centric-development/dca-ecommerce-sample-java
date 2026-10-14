package dev.domaincentric.sample.ecommerce.account.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.account.domain.specification.UsableDateOfBirth;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for the {@link Owner} Value Object. */
@DisplayName("Owner")
class OwnerTest {

  private static final LocalDate DATE_OF_BIRTH = LocalDate.of(1990, 5, 17);

  @Test
  @DisplayName("keeps the names as the person writes them")
  void keepsNamesAsWritten() {
    final Owner owner = Owner.of("jane", "van der Berg", DATE_OF_BIRTH);

    assertEquals("jane", owner.firstName());
    assertEquals("van der Berg", owner.lastName());
  }

  @Test
  @DisplayName("trims surrounding whitespace from the names")
  void trimsNames() {
    final Owner owner = Owner.of("  Jane  ", "  Doe  ", DATE_OF_BIRTH);

    assertEquals("Jane", owner.firstName());
    assertEquals("Doe", owner.lastName());
  }

  @Test
  @DisplayName("the full name is the first and the last name joined by one space")
  void fullNameJoinsFirstAndLastName() {
    final Owner owner = Owner.of("  Anna ", " Schmidt  ", DATE_OF_BIRTH);

    assertEquals("Anna Schmidt", owner.fullName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  @DisplayName("refuses a blank first name")
  void refusesBlankFirstName(final String blank) {
    assertThrows(IllegalArgumentException.class, () -> Owner.of(blank, "Doe", DATE_OF_BIRTH));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  @DisplayName("refuses a blank last name")
  void refusesBlankLastName(final String blank) {
    assertThrows(IllegalArgumentException.class, () -> Owner.of("Jane", blank, DATE_OF_BIRTH));
  }

  @Test
  @DisplayName("refuses a name longer than the allowed maximum")
  void refusesOverlongName() {
    final String tooLong = "x".repeat(101);

    assertThrows(IllegalArgumentException.class, () -> Owner.of(tooLong, "Doe", DATE_OF_BIRTH));
  }

  @Test
  @DisplayName("refuses a missing date of birth")
  void refusesMissingDateOfBirth() {
    assertThrows(IllegalArgumentException.class, () -> Owner.of("Jane", "Doe", null));
  }

  @Test
  @DisplayName("refuses a date of birth in the future")
  void refusesFutureDateOfBirth() {
    final LocalDate tomorrow = LocalDate.now().plusDays(1);

    assertThrows(IllegalArgumentException.class, () -> Owner.of("Jane", "Doe", tomorrow));
  }

  @Test
  @DisplayName("accepts today as a date of birth")
  void acceptsToday() {
    assertEquals(LocalDate.now(), Owner.of("Jane", "Doe", LocalDate.now()).dateOfBirth());
  }

  @Test
  @DisplayName("correcting the date of birth carries both names over unchanged")
  void withDateOfBirthKeepsNames() {
    final Owner owner = Owner.of("Jane", "Doe", DATE_OF_BIRTH);

    final Owner corrected = owner.withDateOfBirth(LocalDate.of(1990, 5, 18));

    assertEquals("Jane", corrected.firstName());
    assertEquals("Doe", corrected.lastName());
    assertEquals(LocalDate.of(1990, 5, 18), corrected.dateOfBirth());
  }

  @Test
  @DisplayName("correcting the date of birth leaves the original owner untouched")
  void withDateOfBirthDoesNotMutate() {
    final Owner owner = Owner.of("Jane", "Doe", DATE_OF_BIRTH);

    owner.withDateOfBirth(LocalDate.of(1990, 5, 18));

    assertEquals(DATE_OF_BIRTH, owner.dateOfBirth());
  }

  @Test
  @DisplayName("delegates the date of birth rule to the specification that owns it")
  void delegatesDateOfBirthRule() {
    final LocalDate tomorrow = LocalDate.now().plusDays(1);

    assertFalse(
        UsableDateOfBirth.RULE.isSatisfiedBy(tomorrow),
        "precondition: the specification refuses a future date");
    assertThrows(IllegalArgumentException.class, () -> Owner.of("Jane", "Doe", tomorrow));
  }
}
