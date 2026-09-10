package dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for the low stock overview an operator watches.
 *
 * <p><b>Hexagonal Architecture:</b> This is a driving/primary port for a read operation.
 *
 * @see GetLowStockProductsUseCase
 */
public interface GetLowStockProductsInputPort
    extends UseCase<GetLowStockProductsQuery, GetLowStockProductsResult> {

  /**
   * Lists the products whose available quantity is lower than the query's threshold.
   *
   * <p>A product with exactly the threshold on hand is not part of the answer, and an answer
   * without any product is empty rather than an error.
   *
   * @param query the query carrying the threshold to compare against
   * @return the products running low, each with the quantity still on hand
   */
  @Override
  GetLowStockProductsResult execute(GetLowStockProductsQuery query);
}
