package dev.domaincentric.sample.ecommerce.cart.application.createcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for creating a new shopping cart.
 *
 * <p>This port defines the contract for creating shopping carts in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see dev.domaincentric.sample.ecommerce.cart.application.createcart.CreateCartUseCase
 */
public interface CreateCartInputPort extends UseCase<CreateCartCommand, CreateCartResult> {

  /**
   * Creates a new shopping cart for a customer.
   *
   * @param command the command containing customer ID
   * @return response containing the created cart details
   */
  @Override
  CreateCartResult execute(CreateCartCommand command);
}
