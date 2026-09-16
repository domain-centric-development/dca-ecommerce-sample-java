package dev.domaincentric.sample.ecommerce.account.adapter.outgoing.security;

import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;

/**
 * JWT-based implementation of identity types.
 *
 * <p>A user starts as ANONYMOUS on first visit and becomes REGISTERED after creating an account.
 */
public enum JwtIdentityType implements IdentityProvider.IdentityType {

  /** Anonymous user with soft identity (JWT without account). */
  ANONYMOUS,

  /** Registered user with hard identity (JWT linked to account). */
  REGISTERED;

  @Override
  public boolean isAnonymous() {
    return this == ANONYMOUS;
  }

  @Override
  public boolean isRegistered() {
    return this == REGISTERED;
  }
}
