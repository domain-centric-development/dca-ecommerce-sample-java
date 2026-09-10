package dev.domaincentric.sample.ecommerce.checkout.application.cartsync.synccheckoutwithcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Legacy cart-change compatibility contract. Snapshot checkout never synchronizes sessions; only an
 * explicit checkout action creates a new snapshot.
 */
public interface SyncCheckoutWithCartInputPort
    extends UseCase<SyncCheckoutWithCartCommand, SyncCheckoutWithCartResult> {

  /**
   * Legacy cart-change compatibility contract. Snapshot checkout never synchronizes sessions; only
   * an explicit checkout action creates a new snapshot.
   */
  @Override
  SyncCheckoutWithCartResult execute(SyncCheckoutWithCartCommand command);
}
