package dev.domaincentric.sample.ecommerce.product.application.getproductbyid;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;

/**
 * What reading one product came to: the product, or no product under that identity.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct) → Controller → ViewModel → Template
 */
public sealed interface GetProductByIdResult
    permits GetProductByIdResult.Found, GetProductByIdResult.ProductNotFound {

  /**
   * The product was found.
   *
   * <p>Wraps an {@link EnrichedProduct} domain read model that combines product state from the
   * Product aggregate with external data from the Pricing and Inventory bounded contexts.
   *
   * @param product the enriched product
   */
  record Found(EnrichedProduct product) implements GetProductByIdResult {}

  /**
   * The catalog carries no product under that identity.
   *
   * @param productId the product the caller named
   */
  record ProductNotFound(ProductId productId) implements GetProductByIdResult, Refusal {}
}
