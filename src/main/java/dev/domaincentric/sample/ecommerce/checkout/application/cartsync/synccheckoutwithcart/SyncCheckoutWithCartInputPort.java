package dev.domaincentric.sample.ecommerce.checkout.application.synccheckoutwithcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for synchronizing a checkout session with current cart state.
 *
 * <p>This port is used when cart contents change during an active checkout session. It updates the
 * checkout session's line items to match the current cart state.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations, typically
 * invoked by event consumers when cart events occur.
 */
public interface SyncCheckoutWithCartInputPort
    extends UseCase<SyncCheckoutWithCartCommand, SyncCheckoutWithCartResult> {

  /**
   * Synchronizes the active checkout session for a cart with current cart state.
   *
   * <p>If there is no active checkout session for the cart, this operation is a no-op.
   *
   * @param command the command containing the cart ID
   * @return response indicating if sync occurred and the updated state
   */
  @Override
  SyncCheckoutWithCartResult execute(SyncCheckoutWithCartCommand command);
}
