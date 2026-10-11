package dev.domaincentric.sample.ecommerce.account.domain.model;

/**
 * The part of the password policy a plaintext password breaks — what {@link
 * PasswordTooWeakException} names, so that whoever shows the refusal can word it.
 */
public enum PasswordRule {
  /** Shorter than {@link HashedPassword#MIN_LENGTH} characters. */
  TOO_SHORT,
  /** Longer than {@link HashedPassword#MAX_BYTE_LENGTH} bytes, UTF-8 encoded. */
  TOO_LONG,
  /** No uppercase letter. */
  NO_UPPERCASE_LETTER,
  /** No lowercase letter. */
  NO_LOWERCASE_LETTER,
  /** No digit. */
  NO_DIGIT
}
