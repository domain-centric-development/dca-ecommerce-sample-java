package dev.domaincentric.sample.ecommerce.product.application.getproductbyid;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for retrieving a product by its ID.
 *
 * <p>This port defines the contract for querying a specific product in the Product bounded context.
 * Primary adapters (REST controllers, MCP tools, etc.) depend on this interface.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for read operations.
 *
 * @see dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdUseCase
 */
public interface GetProductByIdInputPort
    extends UseCase<GetProductByIdQuery, GetProductByIdResult> {

  /**
   * Retrieves a product by its unique identifier.
   *
   * @param query the query containing the product ID
   * @return {@link GetProductByIdResult.Found} with the product, or {@link
   *     GetProductByIdResult.ProductNotFound} when there is no product under that identity
   */
  @Override
  GetProductByIdResult execute(GetProductByIdQuery query);
}
