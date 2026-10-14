package dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem;

/**
 * Command to take one line out of one customer's shopping cart, named by the line's own id.
 *
 * <p>The ids are checked where they become values: {@code CartId}, {@code CustomerId} and {@code
 * CartItemId} refuse a blank one.
 *
 * @param cartId the ID of the cart
 * @param customerId the customer the caller is acting as
 * @param cartItemId the ID of the line to remove
 */
public record RemoveCartItemCommand(String cartId, String customerId, String cartItemId) {}
