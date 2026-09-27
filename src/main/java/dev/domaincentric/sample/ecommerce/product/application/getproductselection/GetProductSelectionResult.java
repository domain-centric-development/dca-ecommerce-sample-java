package dev.domaincentric.sample.ecommerce.product.application.getproductselection;

import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import java.util.List;

/**
 * Output model of the product selection: the drawn products with price and stock, in draw order.
 *
 * @param products the drawn products
 */
public record GetProductSelectionResult(List<EnrichedProduct> products) {

  public GetProductSelectionResult {
    products = List.copyOf(products);
  }
}
