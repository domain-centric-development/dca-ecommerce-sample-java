package dev.domaincentric.sample.ecommerce.account.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;

/**
 * Open Host Service for the caller's identity.
 *
 * <p>The Account context is the only one that establishes who is making a request — from the
 * identity and session cookies or the Bearer header. Every other context keys its data on that
 * {@code UserId}, so this service publishes the resolved identity for their incoming adapters. An
 * adapter reads it once per request and passes the customer into its command or query; the use
 * cases themselves never depend on it.
 *
 * <p>Implemented by the Account context's security adapter, which reads what the authentication
 * filter resolved for the current request.
 */
@OpenHostService(
    context = "Account",
    description = "The identity of the current caller for the incoming adapters of other contexts")
public interface IdentityService {

  /**
   * The identity of the current request.
   *
   * <p>Never empty: a visitor who has not authenticated is anonymous, not absent.
   *
   * @return the caller's identity
   * @throws IllegalStateException if called outside a request the Account context authenticated
   */
  Identity currentIdentity();
}
