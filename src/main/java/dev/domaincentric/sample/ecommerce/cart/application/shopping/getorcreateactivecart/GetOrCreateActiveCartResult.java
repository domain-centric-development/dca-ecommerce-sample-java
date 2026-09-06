package dev.domaincentric.sample.ecommerce.cart.application.getorcreateactivecart;

/**
 * Output model for getting or creating an active cart.
 *
 * @param cartId the cart ID
 * @param customerId the customer ID
 * @param wasCreated whether a new cart was created (true) or existing was found (false)
 */
public record GetOrCreateActiveCartResult(String cartId, String customerId, boolean wasCreated) {}
