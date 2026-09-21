package dev.domaincentric.sample.ecommerce.account.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the password policy encoded in {@link HashedPassword}.
 *
 * <p>Focus is the length bounds, in particular the maximum: it is measured in UTF-8 bytes so that
 * no password can reach the hashing adapter and be rejected there (BCrypt bounds its input at
 * {@value HashedPassword#MAX_BYTE_LENGTH} bytes).
 */
@DisplayName("HashedPassword password policy")
class HashedPasswordTest {

  @Test
  @DisplayName("accepts a password exactly at the maximum byte length")
  void acceptsPasswordAtMaximum() {
    final String atLimit = passwordOfBytes(HashedPassword.MAX_BYTE_LENGTH);

    assertEquals(HashedPassword.MAX_BYTE_LENGTH, utf8Length(atLimit));
    assertDoesNotThrow(() -> HashedPassword.validatePasswordStrength(atLimit));
  }

  @Test
  @DisplayName("rejects a password one byte over the maximum")
  void rejectsPasswordOneByteOverMaximum() {
    final String overLimit = passwordOfBytes(HashedPassword.MAX_BYTE_LENGTH + 1);

    final PasswordTooWeakException thrown =
        assertThrows(
            PasswordTooWeakException.class,
            () -> HashedPassword.validatePasswordStrength(overLimit));
    assertEquals(
        "Password must not be longer than "
            + HashedPassword.MAX_BYTE_LENGTH
            + " bytes (UTF-8 encoded)",
        thrown.getMessage());
  }

  @Test
  @DisplayName("measures the maximum in bytes, not characters")
  void measuresMaximumInBytes() {
    // 30 four-byte emoji = 120 bytes in 60 chars: well under the limit counted as characters,
    // well over it counted as bytes. A character-based rule would let this reach the hasher.
    final String multiByte = "Aa1" + "😀".repeat(30);

    assertTrue(multiByte.length() < HashedPassword.MAX_BYTE_LENGTH, "fewer chars than the limit");
    assertTrue(utf8Length(multiByte) > HashedPassword.MAX_BYTE_LENGTH, "more bytes than the limit");
    assertThrows(
        PasswordTooWeakException.class,
        () -> HashedPassword.validatePasswordStrength(multiByte),
        "a password under the limit in characters but over it in bytes must be rejected");
  }

  @Test
  @DisplayName("rejects a password below the minimum length")
  void rejectsPasswordBelowMinimum() {
    final PasswordTooWeakException thrown =
        assertThrows(
            PasswordTooWeakException.class,
            () -> HashedPassword.validatePasswordStrength("Ab1cdef"));
    assertEquals(
        "Password must be at least " + HashedPassword.MIN_LENGTH + " characters long",
        thrown.getMessage());
  }

  @Test
  @DisplayName("length is checked before the character classes")
  void lengthIsCheckedBeforeCharacterClasses() {
    // All-lowercase and over the byte limit: the length message must win, otherwise an over-long
    // password would be reported as a missing-uppercase problem.
    final String overLimitAllLowercase = "a".repeat(HashedPassword.MAX_BYTE_LENGTH + 1);

    final PasswordTooWeakException thrown =
        assertThrows(
            PasswordTooWeakException.class,
            () -> HashedPassword.validatePasswordStrength(overLimitAllLowercase));
    assertTrue(
        thrown.getMessage().contains("must not be longer than"),
        "expected the length message, was: " + thrown.getMessage());
  }

  /** Builds an ASCII password of exactly {@code bytes} bytes that satisfies every other rule. */
  private static String passwordOfBytes(final int bytes) {
    return "Aa1" + "x".repeat(bytes - 3);
  }

  private static int utf8Length(final String value) {
    return value.getBytes(StandardCharsets.UTF_8).length;
  }
}
