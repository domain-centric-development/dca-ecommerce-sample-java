package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when the article facts do not cover the quantity a customer wants to put in the cart.
 *
 * <p>The figure is a snapshot the cart read through its port, not the warehouse's own decision —
 * the stock keeping unit refuses a shipment it cannot cover with its own rule. This check makes the
 * refusal visible while the customer is still shopping.
 */
public final class InsufficientArticleStockException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;
  private final int requested;

  public InsufficientArticleStockException(final ProductId productId, final int requested) {
    super("Insufficient stock for product: " + productId.value());
    this.productId = productId;
    this.requested = requested;
  }

  public ProductId productId() {
    return productId;
  }

  public int requested() {
    return requested;
  }
}
