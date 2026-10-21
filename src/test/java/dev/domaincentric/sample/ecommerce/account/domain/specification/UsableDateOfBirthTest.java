package dev.domaincentric.sample.ecommerce.account.domain.specification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link UsableDateOfBirth} specification.
 *
 * <p>Pins both halves of the rule and that the enforcing form names which half failed — the message
 * is shown to the user verbatim.
 */
@DisplayName("UsableDateOfBirth")
class UsableDateOfBirthTest {

  @Test
  @DisplayName("a past date satisfies the rule")
  void pastDateIsSatisfying() {
    assertTrue(UsableDateOfBirth.RULE.isSatisfiedBy(LocalDate.of(1990, 5, 17)));
  }

  @Test
  @DisplayName("yesterday satisfies the rule")
  void yesterdayIsSatisfying() {
    assertTrue(UsableDateOfBirth.RULE.isSatisfiedBy(LocalDate.now().minusDays(1)));
  }

  @Test
  @DisplayName("today does not satisfy the rule")
  void todayIsNotSatisfying() {
    assertFalse(UsableDateOfBirth.RULE.isSatisfiedBy(LocalDate.now()));
  }

  @Test
  @DisplayName("enforcing refuses today")
  void enforcingRefusesToday() {
    assertThrows(
        IllegalArgumentException.class,
        () -> UsableDateOfBirth.RULE.requireSatisfiedBy(LocalDate.now()));
  }

  @Test
  @DisplayName("a future date does not satisfy the rule")
  void futureDateIsNotSatisfying() {
    assertFalse(UsableDateOfBirth.RULE.isSatisfiedBy(LocalDate.now().plusDays(1)));
  }

  @Test
  @DisplayName("a missing date does not satisfy the rule")
  void missingDateIsNotSatisfying() {
    assertFalse(UsableDateOfBirth.RULE.isSatisfiedBy(null));
  }

  @Test
  @DisplayName("enforcing accepts a date that satisfies the rule")
  void enforcingAcceptsUsableDate() {
    assertDoesNotThrow(() -> UsableDateOfBirth.RULE.requireSatisfiedBy(LocalDate.of(1990, 5, 17)));
  }

  @Test
  @DisplayName("enforcing reports a missing date as missing")
  void enforcingNamesMissingDate() {
    final IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class, () -> UsableDateOfBirth.RULE.requireSatisfiedBy(null));

    assertTrue(
        e.getMessage().contains("required"),
        "the user must learn which half of the rule failed: " + e.getMessage());
  }

  @Test
  @DisplayName("enforcing reports a future date as being in the future")
  void enforcingNamesFutureDate() {
    final IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> UsableDateOfBirth.RULE.requireSatisfiedBy(LocalDate.now().plusDays(1)));

    assertTrue(
        e.getMessage().contains("future"),
        "the user must learn which half of the rule failed: " + e.getMessage());
  }
}
