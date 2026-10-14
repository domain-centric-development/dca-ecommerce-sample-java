package dev.domaincentric.sample.ecommerce.inventory.application.provisioninitialstock;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for setting or updating stock levels.
 *
 * <p>This port defines the contract for managing stock levels in the Inventory bounded context. If
 * no stock level exists for the product, a new one is created. If a stock level already exists, it
 * is updated to the specified quantity.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for write operations.
 *
 * @see ProvisionInitialStockUseCase
 */
public interface ProvisionInitialStockInputPort
    extends UseCase<ProvisionInitialStockCommand, ProvisionInitialStockResult> {

  /**
   * Sets or updates the stock level for a product.
   *
   * <p>If no stock level exists for the product, a new stock level record is created. If a stock
   * level already exists, it is updated to the new quantity.
   *
   * @param command the command containing product ID and quantity
   * @return result containing the stock level details and whether it was created or updated
   * @throws IllegalArgumentException if quantity is negative
   */
  @Override
  ProvisionInitialStockResult execute(ProvisionInitialStockCommand command);
}
