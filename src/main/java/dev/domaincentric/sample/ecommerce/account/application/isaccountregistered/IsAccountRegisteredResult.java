package dev.domaincentric.sample.ecommerce.account.application.isaccountregistered;

/**
 * Output model of the Is Account Registered use case.
 *
 * @param registered whether an account exists for the queried user id
 */
public record IsAccountRegisteredResult(boolean registered) {}
