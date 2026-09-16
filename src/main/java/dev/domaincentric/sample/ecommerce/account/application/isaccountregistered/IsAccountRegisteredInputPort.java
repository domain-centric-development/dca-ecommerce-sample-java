package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port answering whether a registered account exists for an identity.
 *
 * <p>A session token is self-contained and outlives the account it names: an account that was
 * deleted — or never survived the restart of a store that does not persist — leaves a token that
 * still validates and still carries roles. The authentication adapter asks this port before
 * honouring such a token.
 */
public interface IsAccountRegisteredInputPort
    extends UseCase<IsAccountRegisteredQuery, IsAccountRegisteredResult> {}
