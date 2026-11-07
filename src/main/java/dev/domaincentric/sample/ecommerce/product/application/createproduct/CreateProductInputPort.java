package dev.domaincentric.sample.ecommerce.product.application.createproduct;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for creating a new product.
 *
 * <p>This port defines the contract for creating products in the Product bounded context. Primary
 * adapters (REST controllers, MCP tools, etc.) depend on this interface, not the concrete use case
 * implementation.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port - it drives the application core
 * to execute business logic.
 *
 * @see dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductUseCase
 */
public interface CreateProductInputPort extends UseCase<CreateProductCommand, CreateProductResult> {

  /**
   * Creates a new product.
   *
   * @param command the command containing product details
   * @return {@link CreateProductResult.Created} with the created product, or {@link
   *     CreateProductResult.DuplicateSku} when the catalog already carries the stock keeping unit
   * @throws IllegalArgumentException if a value of the command does not parse
   */
  @Override
  CreateProductResult execute(CreateProductCommand command);
}
