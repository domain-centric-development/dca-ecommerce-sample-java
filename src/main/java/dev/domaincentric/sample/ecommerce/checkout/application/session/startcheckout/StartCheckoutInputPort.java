package dev.domaincentric.sample.ecommerce.checkout.application.startcheckout;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for starting a checkout session from a cart.
 *
 * <p>This port defines the contract for initiating the checkout process. Primary adapters (REST
 * controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see StartCheckoutUseCase
 */
public interface StartCheckoutInputPort extends UseCase<StartCheckoutCommand, StartCheckoutResult> {

  /**
   * Starts a checkout session from the specified cart.
   *
   * <p>This operation:
   *
   * <ul>
   *   <li>Loads the cart and validates it can be checked out
   *   <li>Creates a new checkout session with line items from the cart
   *   <li>Marks the cart as checked out
   *   <li>Persists the checkout session
   * </ul>
   *
   * @param command the command containing the cart ID
   * @return response containing the created checkout session details
   * @throws IllegalArgumentException if cart is not found or cannot be checked out
   */
  @Override
  StartCheckoutResult execute(StartCheckoutCommand command);
}
