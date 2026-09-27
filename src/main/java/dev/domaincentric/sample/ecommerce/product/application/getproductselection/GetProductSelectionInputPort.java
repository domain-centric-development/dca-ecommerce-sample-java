package dev.domaincentric.sample.ecommerce.product.application.getproductselection;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/** Input port for drawing up to eight random catalogue products that have a price. */
public interface GetProductSelectionInputPort
    extends UseCase<GetProductSelectionQuery, GetProductSelectionResult> {

  @Override
  GetProductSelectionResult execute(GetProductSelectionQuery query);
}
