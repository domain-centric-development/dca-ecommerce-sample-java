package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when the article facts a line item needs are not available for a product.
 *
 * <p>Checkout asks the assortment for name, price and availability of everything in the cart. A
 * product the answer does not carry cannot become a line item, and the totals would otherwise be
 * computed over a gap.
 */
public final class ArticleNotAvailableException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;

  public ArticleNotAvailableException(final ProductId productId) {
    super("Product not found: " + productId.value());
    this.productId = productId;
  }

  public ProductId productId() {
    return productId;
  }
}
