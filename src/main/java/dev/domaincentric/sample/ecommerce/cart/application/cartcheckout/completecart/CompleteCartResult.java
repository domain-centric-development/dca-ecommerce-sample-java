package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;

/**
 * Output model for cart completion.
 *
 * @param cartId the cart ID
 * @param status the cart status after the completion
 * @param alreadyCompleted whether the event had completed the cart before, so nothing changed
 */
public record CompleteCartResult(String cartId, CartStatus status, boolean alreadyCompleted) {}
