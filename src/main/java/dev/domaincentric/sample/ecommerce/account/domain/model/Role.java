package dev.domaincentric.sample.ecommerce.account.domain.model;

import java.util.Arrays;

/**
 * What an account may do beyond shopping: every registered account is a {@link #CUSTOMER}; a {@link
 * #STAFF} account may in addition create products and list every cart.
 *
 * <p>The {@link #code()} is the role's spelling outside the domain — in the token's {@code roles}
 * claim and in the stored role set. It is fixed per value, so renaming a member never changes what
 * a client reads or what the store holds.
 */
public enum Role {
  CUSTOMER("CUSTOMER"),
  STAFF("STAFF");

  private final String code;

  Role(final String code) {
    this.code = code;
  }

  /**
   * The role with that code.
   *
   * @param code a role's code, as {@link #code()} spells it
   * @return the role
   * @throws IllegalArgumentException if no role has that code
   */
  public static Role fromCode(final String code) {
    return Arrays.stream(values())
        .filter(role -> role.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + code));
  }

  public String code() {
    return code;
  }
}
