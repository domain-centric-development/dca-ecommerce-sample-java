package dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart;

/**
 * Command for synchronizing a checkout session with current cart state.
 *
 * @param cartId the ID of the cart that changed
 */
public record SyncCheckoutWithCartCommand(String cartId) {}
