package dev.domaincentric.sample.ecommerce.product.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductResult;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming between use case models and DTOs.
 *
 * <p><b>Adapter Pattern:</b> This class belongs to the adapter layer and handles the translation
 * between application layer models (which wrap domain read models) and REST API DTOs.
 *
 * <p>Use cases return Results containing EnrichedProduct domain read models. This converter maps
 * those to flat DTOs suitable for JSON serialization.
 */
@Component
public final class ProductDtoConverter {

  /**
   * Converts the created product to DTO.
   *
   * <p>Note: Stock information is not available on a created product as it's a write operation.
   *
   * @param output the use case output
   * @return product DTO
   */
  public ProductDto toDto(final CreateProductResult.Created output) {
    return new ProductDto(
        output.productId(),
        output.sku(),
        output.name(),
        output.description(),
        output.imageUrl(),
        output.priceAmount(),
        output.priceCurrency(),
        output.category(),
        null,
        null);
  }

  /**
   * Converts EnrichedProduct domain read model to DTO.
   *
   * @param product the enriched product
   * @return product DTO
   */
  public ProductDto toDto(final EnrichedProduct product) {
    return new ProductDto(
        product.productId().value().toString(),
        product.sku(),
        product.name(),
        product.description(),
        product.imageUrl(),
        product.currentPrice().amount(),
        product.currentPrice().currency().getCurrencyCode(),
        product.category(),
        product.stockQuantity(),
        product.isAvailable());
  }
}
