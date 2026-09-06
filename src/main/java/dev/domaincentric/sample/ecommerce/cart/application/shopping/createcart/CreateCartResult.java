package dev.domaincentric.sample.ecommerce.cart.application.createcart;

/**
 * Output model for cart creation.
 *
 * @param cartId the generated cart ID
 * @param customerId the customer ID
 * @param status the cart status (always "ACTIVE" for new carts)
 */
public record CreateCartResult(String cartId, String customerId, String status) {}
