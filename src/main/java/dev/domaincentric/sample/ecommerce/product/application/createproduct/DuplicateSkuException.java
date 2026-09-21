package dev.domaincentric.sample.ecommerce.product.application.createproduct;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import java.io.Serial;

/**
 * Raised when a product is created with a stock keeping unit the catalog already carries.
 *
 * <p>Uniqueness is not an invariant a single product can hold — it is a statement about the whole
 * catalog, checked against the repository — so the failure belongs to the use case, not to the
 * aggregate. A malformed stock keeping unit is a different failure: the value object refuses it as
 * an argument, and the caller has to send something else, not something new.
 */
public final class DuplicateSkuException extends UseCaseException {

  @Serial private static final long serialVersionUID = 1L;

  private final SKU sku;

  public DuplicateSkuException(final SKU sku) {
    super("Product with SKU already exists: " + sku.value());
    this.sku = sku;
  }

  public SKU sku() {
    return sku;
  }
}
