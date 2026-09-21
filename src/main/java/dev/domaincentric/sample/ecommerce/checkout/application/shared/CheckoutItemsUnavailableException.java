package dev.domaincentric.sample.ecommerce.checkout.application.shared;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.io.Serial;
import java.util.List;

/**
 * Raised when a checkout would start although some of the cart's positions cannot be bought.
 *
 * <p>Out of stock, withdrawn from the assortment, priced differently — the enriched read model
 * answers that per position, and the customer is told which ones before a session exists.
 */
public final class CheckoutItemsUnavailableException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final List<ProductId> productIds;

  public CheckoutItemsUnavailableException(final List<ProductId> productIds) {
    super("Cannot start checkout, " + productIds.size() + " item(s) unavailable or out of stock");
    this.productIds = List.copyOf(productIds);
  }

  /** The positions that refused, in cart order. */
  public List<ProductId> productIds() {
    return productIds;
  }
}
