package dev.domaincentric.sample.ecommerce.account.application.changeprofile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.account.application.changeprofile.ChangeProfileResult.Profile;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link ChangeProfileResult}.
 *
 * <p>Pins that every rejection is a case of the result rather than an exception crossing the port,
 * and that the result cannot be built in a state that contradicts its case. A rejection carries no
 * words: the page words it from the case.
 */
@DisplayName("ChangeProfileResult")
class ChangeProfileResultTest {

  private static final Profile PROFILE =
      new Profile("jane.doe@example.com", LocalDate.of(1990, 5, 17));

  @Test
  @DisplayName("a change carries the stored profile")
  void changedCarriesProfile() {
    final ChangeProfileResult result = new ChangeProfileResult.Changed(PROFILE);

    assertEquals(PROFILE, assertInstanceOf(ChangeProfileResult.Changed.class, result).profile());
    assertFalse(result instanceof Refusal, "a stored change is no refusal");
  }

  @Test
  @DisplayName("a change without the stored profile is refused")
  void changeNeedsProfile() {
    assertThrows(IllegalArgumentException.class, () -> new ChangeProfileResult.Changed(null));
  }

  static Stream<ChangeProfileResult> rejections() {
    return Stream.of(
        new ChangeProfileResult.EmailAlreadyInUse(),
        new ChangeProfileResult.EmailInvalid(),
        new ChangeProfileResult.DateOfBirthInvalid());
  }

  @ParameterizedTest
  @MethodSource("rejections")
  @DisplayName("a rejection is a refusal and carries no profile")
  void rejectionCarriesNoProfile(final ChangeProfileResult rejection) {
    assertTrue(rejection instanceof Refusal, "a rejection rolls back like a thrown failure");
    assertEquals(0, rejection.getClass().getRecordComponents().length, "no profile, no words");
  }

  @Test
  @DisplayName("an inaccessible account carries no profile")
  void accountNotAccessibleCarriesNothing() {
    final ChangeProfileResult result = new ChangeProfileResult.AccountNotAccessible();

    assertTrue(result instanceof Refusal);
    assertEquals(0, result.getClass().getRecordComponents().length);
  }
}
