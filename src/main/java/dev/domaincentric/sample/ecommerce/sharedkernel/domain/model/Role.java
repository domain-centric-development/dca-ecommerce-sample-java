package dev.domaincentric.sample.ecommerce.sharedkernel.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import java.util.Set;

/**
 * What an account may do beyond shopping. A value from a fixed set: the only roles are {@link
 * #CUSTOMER} and {@link #STAFF}, so no other role can be held, granted or asked for.
 *
 * <p>It lives in the shared kernel because the account context grants roles and the adapters of
 * other contexts — the product and cart APIs — guard on them through the identity that carries
 * them. The {@link #code()} is the role's spelling outside the domain — in the token's {@code
 * roles} claim and in the stored role set — so renaming a constant never changes what a client
 * reads or what the store holds.
 *
 * @param code the role's published code
 */
public record Role(String code) implements Value {

  /** The role every account gets at registration. */
  public static final Role CUSTOMER = new Role("CUSTOMER");

  /**
   * The operator role. It guards what a shopper must never reach — listing every customer's cart,
   * creating a product — and no registration path hands it out: an account only gets it by being
   * given it.
   */
  public static final Role STAFF = new Role("STAFF");

  /**
   * Refuses a code no role has, so a role built by hand is always one of the set.
   *
   * @throws IllegalArgumentException if no role has that code
   */
  public Role {
    if (!"CUSTOMER".equals(code) && !"STAFF".equals(code)) {
      throw new IllegalArgumentException("Unknown role: " + code);
    }
  }

  /**
   * Every role there is.
   *
   * @return the set of roles
   */
  public static Set<Role> all() {
    return Set.of(CUSTOMER, STAFF);
  }

  /**
   * The role with that code.
   *
   * @param code a role's code, as {@link #code()} spells it
   * @return the role
   * @throws IllegalArgumentException if no role has that code
   */
  public static Role fromCode(final String code) {
    return all().stream()
        .filter(role -> role.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + code));
  }

  @Override
  public String toString() {
    return code;
  }
}
