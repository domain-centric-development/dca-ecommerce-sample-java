package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.in.UseCase;

/**
 * Input port for changing a product's price: the current price is replaced by a new one, effective
 * now.
 *
 * @see ChangePriceUseCase
 */
public interface ChangePriceInputPort extends UseCase<ChangePriceCommand, ChangePriceResult> {

  /**
   * Changes a product's price.
   *
   * @param command the product and its new amount
   * @return the price as it is now
   * @throws ProductPriceNotFoundException if Pricing holds no price for the product
   */
  @Override
  ChangePriceResult execute(ChangePriceCommand command);
}
