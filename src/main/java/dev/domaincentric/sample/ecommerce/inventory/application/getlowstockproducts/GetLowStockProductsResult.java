package dev.domaincentric.sample.ecommerce.inventory.application.getlowstockproducts;

import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * Output model listing the products that are running low.
 *
 * @param products the products below the threshold, empty when none is
 */
public record GetLowStockProductsResult(List<LowStockProduct> products) {

  public GetLowStockProductsResult {
    if (products == null) {
      throw new IllegalArgumentException("Products cannot be null");
    }
    products = List.copyOf(products);
  }

  /**
   * A single product that is running low.
   *
   * @param productId the product identifier
   * @param availableQuantity the quantity still on hand, regardless of what is already reserved
   */
  public record LowStockProduct(ProductId productId, int availableQuantity) {

    public LowStockProduct {
      if (productId == null) {
        throw new IllegalArgumentException("ProductId cannot be null");
      }
      if (availableQuantity < 0) {
        throw new IllegalArgumentException("Available quantity cannot be negative");
      }
    }
  }
}
