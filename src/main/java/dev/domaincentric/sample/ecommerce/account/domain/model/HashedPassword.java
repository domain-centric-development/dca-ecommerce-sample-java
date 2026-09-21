package dev.domaincentric.sample.ecommerce.account.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.account.domain.gateway.PasswordHasher;
import java.nio.charset.StandardCharsets;

/**
 * Value Object representing a securely hashed password.
 *
 * <p>This value object:
 *
 * <ul>
 *   <li>Never stores plaintext passwords
 *   <li>Validates password strength requirements on construction from plaintext
 *   <li>Delegates the actual hashing/verification to a {@link PasswordHasher} domain gateway
 * </ul>
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * // Create from plaintext (validates strength, hashes via gateway)
 * HashedPassword password = HashedPassword.fromPlaintext("MyP@ssw0rd!", hasher);
 *
 * // Verify password
 * boolean valid = password.matches("MyP@ssw0rd!", hasher);
 *
 * // Restore from persistence
 * HashedPassword restored = HashedPassword.of("$2a$12$...");
 * }</pre>
 *
 * <p>{@link #toString()} never reveals the hash.
 */
public record HashedPassword(String hash) implements Value {

  /** Minimum password length requirement. */
  public static final int MIN_LENGTH = 8;

  /**
   * Maximum password length requirement, in UTF-8 bytes.
   *
   * <p>Measured in bytes rather than characters because that is what hashing algorithms bound:
   * BCrypt silently truncates or rejects input beyond 72 bytes. Keeping the domain limit at that
   * value means the policy decides which passwords are acceptable and the hashing adapter never has
   * to — a longer password is rejected here, as a rule, instead of failing inside {@link
   * PasswordHasher} where it would surface as a fault.
   */
  public static final int MAX_BYTE_LENGTH = 72;

  public HashedPassword {
    if (hash == null || hash.isBlank()) {
      throw new IllegalArgumentException("Password hash cannot be null or blank");
    }
  }

  /**
   * Creates a HashedPassword from plaintext by validating strength and hashing via the gateway.
   *
   * @param plaintext the plaintext password
   * @param hasher the password hashing domain gateway
   * @return a HashedPassword wrapping the generated hash
   * @throws PasswordTooWeakException if the password does not meet the strength rules
   */
  public static HashedPassword fromPlaintext(final String plaintext, final PasswordHasher hasher) {
    validatePasswordStrength(plaintext);
    return new HashedPassword(hasher.hash(plaintext));
  }

  /**
   * Creates a HashedPassword from an existing hash string.
   *
   * @param hash the existing hash
   * @return a HashedPassword wrapping the hash
   */
  public static HashedPassword of(final String hash) {
    return new HashedPassword(hash);
  }

  /**
   * Alias for {@link #of(String)} - restores a HashedPassword from storage.
   *
   * @param hash the existing hash
   * @return a HashedPassword wrapping the hash
   */
  public static HashedPassword fromHash(final String hash) {
    return of(hash);
  }

  /**
   * Verifies a plaintext password against this hash via the gateway (timing-safe).
   *
   * @param plaintext the plaintext password to verify
   * @param hasher the password hashing domain gateway
   * @return true if the plaintext matches
   */
  public boolean matches(final String plaintext, final PasswordHasher hasher) {
    return hasher.matches(plaintext, hash);
  }

  /**
   * Validates password strength requirements.
   *
   * <p>Requirements:
   *
   * <ul>
   *   <li>Minimum 8 characters
   *   <li>At most {@value #MAX_BYTE_LENGTH} bytes when UTF-8 encoded
   *   <li>At least one uppercase letter
   *   <li>At least one lowercase letter
   *   <li>At least one digit
   * </ul>
   *
   * @param plaintext the password to validate
   * @throws PasswordTooWeakException if the strength rules are not met
   */
  public static void validatePasswordStrength(final String plaintext) {
    if (plaintext == null || plaintext.length() < MIN_LENGTH) {
      throw new PasswordTooWeakException(
          "Password must be at least " + MIN_LENGTH + " characters long");
    }
    if (plaintext.getBytes(StandardCharsets.UTF_8).length > MAX_BYTE_LENGTH) {
      throw new PasswordTooWeakException(
          "Password must not be longer than " + MAX_BYTE_LENGTH + " bytes (UTF-8 encoded)");
    }

    boolean hasUpper = false;
    boolean hasLower = false;
    boolean hasDigit = false;

    for (char c : plaintext.toCharArray()) {
      if (Character.isUpperCase(c)) hasUpper = true;
      if (Character.isLowerCase(c)) hasLower = true;
      if (Character.isDigit(c)) hasDigit = true;
    }

    if (!hasUpper) {
      throw new PasswordTooWeakException("Password must contain at least one uppercase letter");
    }
    if (!hasLower) {
      throw new PasswordTooWeakException("Password must contain at least one lowercase letter");
    }
    if (!hasDigit) {
      throw new PasswordTooWeakException("Password must contain at least one digit");
    }
  }

  /**
   * Returns a masked string representation (never reveals the hash).
   *
   * @return a constant masked string
   */
  @Override
  public String toString() {
    return "HashedPassword[********]";
  }
}
