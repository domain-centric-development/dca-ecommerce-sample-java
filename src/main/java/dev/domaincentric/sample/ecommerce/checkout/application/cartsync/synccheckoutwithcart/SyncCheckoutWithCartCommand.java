package dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart;

/**
 * Legacy cart-change compatibility contract. Snapshot checkout never synchronizes sessions; only an
 * explicit checkout action creates a new snapshot.
 */
public record SyncCheckoutWithCartCommand(String cartId) {}
