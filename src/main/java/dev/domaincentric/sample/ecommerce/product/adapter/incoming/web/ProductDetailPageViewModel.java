package dev.domaincentric.sample.ecommerce.product.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import dev.domaincentric.sample.ecommerce.product.domain.model.EnrichedProduct;
import java.math.BigDecimal;

/**
 * Page-specific ViewModel for the product detail page.
 *
 * <p>This ViewModel contains only primitives and is tailored to the detail page's needs. It is
 * created from the use case result (which wraps an EnrichedProduct domain read model) in the
 * adapter layer.
 *
 * <p><b>Pattern:</b> Use Case → Result(EnrichedProduct) → Controller → ViewModel → Template
 *
 * @param productId the product ID
 * @param sku the SKU
 * @param name the product name
 * @param description the product description
 * @param imageUrl the product image URL
 * @param priceAmount the price amount
 * @param priceCurrency the price currency code
 * @param category the product category
 * @param stockQuantity the available stock
 * @param isAvailable whether the product is available for purchase
 * @param pageTitle the page title
 */
public record ProductDetailPageViewModel(
    String productId,
    String sku,
    String name,
    String description,
    String imageUrl,
    BigDecimal priceAmount,
    String priceCurrency,
    String category,
    int stockQuantity,
    boolean isAvailable,
    String pageTitle) {

  /**
   * Creates a ViewModel from the product the use case found.
   *
   * @param found the use case's case carrying the enriched product
   * @return the page-specific ViewModel
   */
  public static ProductDetailPageViewModel fromResult(final GetProductByIdResult.Found found) {
    final EnrichedProduct product = found.product();

    return new ProductDetailPageViewModel(
        product.productId().value().toString(),
        product.sku(),
        product.name(),
        product.description().isEmpty() ? "No description available." : product.description(),
        product.imageUrl(),
        product.currentPrice().amount(),
        product.currentPrice().currency().getCurrencyCode(),
        product.category(),
        product.stockQuantity(),
        product.isAvailable(),
        product.name());
  }
}
