package dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;

/**
 * Raised when the article facts do not cover what the cart's line would hold: the quantity a
 * customer wants to add beside the quantity the line holds already.
 *
 * <p>The figure is a snapshot the cart read through its port, not the warehouse's own decision —
 * the stock keeping unit refuses a shipment it cannot cover with its own rule. This check makes the
 * refusal visible while the customer is still shopping.
 */
public final class InsufficientArticleStockException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final ProductId productId;
  private final int requested;
  private final int held;
  private final int inStock;

  public InsufficientArticleStockException(
      final ProductId productId, final int requested, final int held, final int inStock) {
    super("Insufficient stock for product: " + productId.value());
    this.productId = productId;
    this.requested = requested;
    this.held = held;
    this.inStock = inStock;
  }

  public ProductId productId() {
    return productId;
  }

  /** The quantity the customer wanted to add. */
  public int requested() {
    return requested;
  }

  /** The quantity the cart's line held already. */
  public int held() {
    return held;
  }

  /** The stock the article facts named. */
  public int inStock() {
    return inStock;
  }
}
