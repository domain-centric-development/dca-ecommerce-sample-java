package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import java.io.Serial;

/**
 * Raised when the shipping option a customer chose is not one the shop offers.
 *
 * <p>The registry is the shop's list of shipping options; an identifier outside it names nothing
 * this checkout can deliver with or price.
 */
public final class ShippingOptionNotOfferedException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final String shippingOptionId;

  public ShippingOptionNotOfferedException(final String shippingOptionId) {
    super("Shipping option not offered: " + shippingOptionId);
    this.shippingOptionId = shippingOptionId;
  }

  public String shippingOptionId() {
    return shippingOptionId;
  }
}
