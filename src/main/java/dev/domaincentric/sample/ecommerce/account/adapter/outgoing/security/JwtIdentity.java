package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.UserId;
import java.util.Optional;
import java.util.Set;

/**
 * JWT-based implementation of Identity.
 *
 * <p>This is an immutable record that carries identity information throughout a request. It is
 * available via {@link IdentityProvider#getCurrentIdentity()}.
 *
 * @param userId the user's unique identifier (always present)
 * @param type the identity type (ANONYMOUS or REGISTERED)
 * @param email the user's email (only present for registered users)
 * @param roles the user's roles (typically empty for anonymous, contains CUSTOMER for registered)
 */
public record JwtIdentity(
    UserId userId, JwtIdentityType type, Optional<String> email, Set<String> roles)
    implements IdentityProvider.Identity {

  public JwtIdentity {
    if (userId == null) {
      throw new IllegalArgumentException("UserId cannot be null");
    }
    if (type == null) {
      throw new IllegalArgumentException("IdentityType cannot be null");
    }
    if (email == null) {
      email = Optional.empty();
    }
    if (roles == null) {
      roles = Set.of();
    }
  }

  /**
   * Creates an anonymous identity.
   *
   * @param userId the anonymous user's ID
   * @return a new anonymous identity
   */
  public static JwtIdentity anonymous(final UserId userId) {
    return new JwtIdentity(userId, JwtIdentityType.ANONYMOUS, Optional.empty(), Set.of());
  }

  /**
   * Creates a registered identity.
   *
   * @param userId the registered user's ID
   * @param email the user's email address
   * @param roles the user's roles
   * @return a new registered identity
   */
  public static JwtIdentity registered(
      final UserId userId, final String email, final Set<String> roles) {
    return new JwtIdentity(userId, JwtIdentityType.REGISTERED, Optional.of(email), roles);
  }

  /**
   * Creates a registered identity with the default CUSTOMER role.
   *
   * @param userId the registered user's ID
   * @param email the user's email address
   * @return a new registered identity with CUSTOMER role
   */
  public static JwtIdentity registeredCustomer(final UserId userId, final String email) {
    return registered(userId, email, Set.of(ROLE_CUSTOMER));
  }
}
