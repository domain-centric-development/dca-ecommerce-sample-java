package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for asking whether an account is registered for a user id.
 *
 * <p>Called by the authentication filter before it honours a session token: the token is
 * self-contained and outlives the account it names, so the question has to be asked of the context
 * that owns accounts. It points inward, which is why it is a query use case and not an output port.
 */
public interface IsAccountRegisteredInputPort
    extends UseCase<IsAccountRegisteredQuery, IsAccountRegisteredResult> {}
