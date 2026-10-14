package dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart;

import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;

/**
 * Output model for cart creation.
 *
 * @param cartId the generated cart ID
 * @param customerId the customer ID
 * @param status the cart status (always {@code ACTIVE} for new carts)
 */
public record CreateCartResult(String cartId, String customerId, CartStatus status) {}
