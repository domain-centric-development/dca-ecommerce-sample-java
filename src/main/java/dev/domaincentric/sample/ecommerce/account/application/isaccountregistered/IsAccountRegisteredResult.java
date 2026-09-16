package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

/**
 * Whether an account exists for the queried identity.
 *
 * @param registered true if an account is linked to the user ID
 */
public record IsAccountRegisteredResult(boolean registered) {}
