package dev.domaincentric.sample.ecommerce.checkout.application.synccheckoutwithcart;

/**
 * Response from synchronizing a checkout session with cart.
 *
 * @param synced true if an active checkout session was found and synced
 * @param sessionId the checkout session ID if synced, null otherwise
 * @param itemCount the new number of line items after sync
 */
public record SyncCheckoutWithCartResult(boolean synced, String sessionId, int itemCount) {

  public static SyncCheckoutWithCartResult noActiveSession() {
    return new SyncCheckoutWithCartResult(false, null, 0);
  }

  public static SyncCheckoutWithCartResult synced(final String sessionId, final int itemCount) {
    return new SyncCheckoutWithCartResult(true, sessionId, itemCount);
  }
}
