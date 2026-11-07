package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

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
   * @return {@link CompleteCartResult.Completed} with the cart's status, or {@link
   *     CompleteCartResult.CartNotFound} when there is no cart under that identity
   * @throws IllegalArgumentException if the command's positions do not parse
   */
  @Override
  CompleteCartResult execute(CompleteCartCommand command);
}
