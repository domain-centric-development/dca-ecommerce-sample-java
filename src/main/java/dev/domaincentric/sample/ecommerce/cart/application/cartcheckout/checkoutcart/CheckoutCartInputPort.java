package dev.domaincentric.sample.ecommerce.cart.application.checkoutcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for checking out a shopping cart.
 *
 * <p>This port defines the contract for cart checkout operations in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see dev.domaincentric.sample.ecommerce.cart.application.checkoutcart.CheckoutCartUseCase
 */
public interface CheckoutCartInputPort extends UseCase<CheckoutCartCommand, CheckoutCartResult> {

  /**
   * Checks out a shopping cart, finalizing the purchase.
   *
   * @param command the command containing cart ID
   * @return response containing checkout confirmation details
   * @throws IllegalArgumentException if cart not found, cart is empty, or cart already checked out
   */
  @Override
  CheckoutCartResult execute(CheckoutCartCommand command);
}
