package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when Pricing holds no price for the addressed product — every product of the catalogue has
 * one, so there is no such product.
 */
public final class ProductPriceNotFoundException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;

  public ProductPriceNotFoundException(final ProductId productId) {
    super("No price for product: " + productId.value());
    this.productId = productId;
  }

  public ProductId productId() {
    return productId;
  }
}
