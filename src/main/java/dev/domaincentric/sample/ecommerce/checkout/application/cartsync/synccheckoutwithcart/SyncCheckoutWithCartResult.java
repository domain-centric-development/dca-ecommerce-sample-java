package dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart;

/**
 * Legacy cart-change compatibility contract. Snapshot checkout never synchronizes sessions; only an
 * explicit checkout action creates a new snapshot.
 */
public record SyncCheckoutWithCartResult(boolean synced, String sessionId, int itemCount) {

  public static SyncCheckoutWithCartResult noActiveSession() {
    return new SyncCheckoutWithCartResult(false, null, 0);
  }

  public static SyncCheckoutWithCartResult synced(final String sessionId, final int itemCount) {
    return new SyncCheckoutWithCartResult(true, sessionId, itemCount);
  }
}
