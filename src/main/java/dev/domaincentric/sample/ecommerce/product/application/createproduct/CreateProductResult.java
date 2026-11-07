package dev.domaincentric.sample.ecommerce.product.application.createproduct;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import java.math.BigDecimal;

/**
 * What creating a product came to: the product was created, or the catalog already carries its
 * stock keeping unit.
 */
public sealed interface CreateProductResult
    permits CreateProductResult.Created, CreateProductResult.DuplicateSku {

  /**
   * The product was created.
   *
   * <p>It exposes only the data needed by the presentation layer, not domain entities. Stock
   * information is not included as stock is managed by the Inventory bounded context.
   *
   * @param productId the generated product ID
   * @param sku the product SKU
   * @param name the product name
   * @param description the product description
   * @param imageUrl the product image URL
   * @param priceAmount the price amount
   * @param priceCurrency the price currency
   * @param category the product category
   */
  record Created(
      String productId,
      String sku,
      String name,
      String description,
      String imageUrl,
      BigDecimal priceAmount,
      String priceCurrency,
      String category)
      implements CreateProductResult {}

  /**
   * The catalog already carries that stock keeping unit. Uniqueness is not an invariant a single
   * product can hold — it is a statement about the whole catalog, checked against the repository —
   * so the refusal belongs to the use case, not to the aggregate. Nothing was written.
   *
   * @param sku the stock keeping unit the request named
   */
  record DuplicateSku(SKU sku) implements CreateProductResult, Refusal {}
}
