package dev.domaincentric.sample.ecommerce.account.api;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.Set;

/**
 * Who the current request belongs to, and what they are allowed to be.
 *
 * <p>Published by {@link IdentityService}. The {@link UserId} is the cross-context identity: it
 * survives session expiry and changes only on explicit logout, so it is the same before and after a
 * login — authentication adds a session, it does not replace who the browser is.
 *
 * @param userId the caller's identity, always present
 * @param type whether the caller has authenticated
 * @param email the account's email, present for a registered caller only
 * @param roles the account's roles, empty for an anonymous caller
 */
public record Identity(
    UserId userId, IdentityType type, Optional<String> email, Set<String> roles) {

  /** The role every registered account holds. */
  public static final String ROLE_CUSTOMER = "CUSTOMER";

  /**
   * Operator role. It guards what a shopper must never reach — listing every customer's cart,
   * creating a product — and no registration path hands it out: an account only gets it by being
   * given it.
   */
  public static final String ROLE_STAFF = "STAFF";

  public Identity {
    if (userId == null) {
      throw new IllegalArgumentException("UserId cannot be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("IdentityType cannot be null");
    }
    email = email == null ? Optional.empty() : email;
    roles = roles == null ? Set.of() : Set.copyOf(roles);
  }

  /** An identity without a session, keeping the {@code UserId} the browser already carries. */
  public static Identity anonymous(final UserId userId) {
    return new Identity(userId, IdentityType.ANONYMOUS, Optional.empty(), Set.of());
  }

  /** The identity of an authenticated account. */
  public static Identity registered(
      final UserId userId, final String email, final Set<String> roles) {
    return new Identity(userId, IdentityType.REGISTERED, Optional.of(email), roles);
  }

  /** The identity of an authenticated account holding the default customer role only. */
  public static Identity registeredCustomer(final UserId userId, final String email) {
    return registered(userId, email, Set.of(ROLE_CUSTOMER));
  }

  public boolean isAnonymous() {
    return type.isAnonymous();
  }

  public boolean isRegistered() {
    return type.isRegistered();
  }

  public boolean hasRole(final String role) {
    return roles.contains(role);
  }
}
