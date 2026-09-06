package dev.domaincentric.sample.ecommerce.cart.application.completecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for completing a shopping cart after checkout confirmation.
 *
 * <p>This port defines the contract for marking a cart as completed after the checkout process has
 * been fully confirmed by the customer.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 */
public interface CompleteCartInputPort extends UseCase<CompleteCartCommand, CompleteCartResult> {

  /**
   * Completes a shopping cart after checkout confirmation.
   *
   * @param command the command containing cart ID
   * @return response containing completion confirmation
   * @throws IllegalArgumentException if cart not found
   * @throws IllegalStateException if cart is not in CHECKED_OUT status
   */
  @Override
  CompleteCartResult execute(CompleteCartCommand command);
}
