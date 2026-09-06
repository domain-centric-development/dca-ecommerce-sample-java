package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

/**
 * Output model for cart completion.
 *
 * @param cartId the cart ID
 * @param status the new cart status (COMPLETED)
 */
public record CompleteCartResult(String cartId, String status) {}
