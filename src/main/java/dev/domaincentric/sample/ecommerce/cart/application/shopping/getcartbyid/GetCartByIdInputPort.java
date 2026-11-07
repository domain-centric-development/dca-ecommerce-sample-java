package dev.domaincentric.sample.ecommerce.cart.application.shopping.getcartbyid;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for retrieving a cart by its ID.
 *
 * <p>This port defines the contract for querying a specific cart in the Cart bounded context.
 * Primary adapters (REST controllers, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see GetCartByIdUseCase
 */
public interface GetCartByIdInputPort extends UseCase<GetCartByIdQuery, GetCartByIdResult> {

  /**
   * Retrieves a shopping cart by its unique identifier.
   *
   * @param query the query containing the cart ID
   * @return {@link GetCartByIdResult.Found} with the enriched cart read model, or {@link
   *     GetCartByIdResult.CartNotFound} when the caller holds no cart under that identity
   */
  @Override
  GetCartByIdResult execute(GetCartByIdQuery query);
}
