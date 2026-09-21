package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when the article facts a cart position needs are not available for a product.
 *
 * <p>The cart does not own the assortment; it asks for the article through its own port. A product
 * the answer does not carry cannot become a position, and that is a statement about the request,
 * not about the cart.
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
