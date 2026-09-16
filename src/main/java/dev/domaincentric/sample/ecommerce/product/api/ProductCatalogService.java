package dev.domaincentric.sample.ecommerce.product.api;

import dev.domaincentric.dca.buildingblocks.ddd.strategic.relationships.OpenHostService;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductCommand;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductInputPort;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductResult;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdQuery;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Open Host Service for Product Catalog.
 *
 * <p>An Open Host Service in {@code api/}: the in-process published contract of the Product
 * context. It delegates to use cases (input ports) and translates responses to its own DTOs.
 *
 * <p>Consuming contexts should NOT use this service directly in their use cases - they should
 * define their own output ports and implement adapters that delegate to this service.
 *
 * <p><b>Hexagonal Architecture:</b> Like an incoming adapter, this service calls input ports (use
 * cases), never output ports (repositories) directly.
 *
 * <p><b>Note:</b> Product context owns identity (productId, sku) and description (name). Pricing is
 * provided by PricingService. Stock/availability is provided by InventoryService.
 */
@OpenHostService(
    context = "Product Catalog",
    description =
        "Provides product identity and description (name, SKU) for other bounded contexts. Pricing is provided by PricingService. Stock is provided by InventoryService.")
@Service
public class ProductCatalogService {

  private final GetProductByIdInputPort getProductByIdInputPort;
  private final GetAllProductsInputPort getAllProductsInputPort;
  private final CreateProductInputPort createProductInputPort;

  public ProductCatalogService(
      GetProductByIdInputPort getProductByIdInputPort,
      GetAllProductsInputPort getAllProductsInputPort,
      CreateProductInputPort createProductInputPort) {
    this.getProductByIdInputPort = getProductByIdInputPort;
    this.getAllProductsInputPort = getAllProductsInputPort;
    this.createProductInputPort = createProductInputPort;
  }

  /**
   * Product information DTO for cross-context communication.
   *
   * <p>Contains only identity and description data owned by Product context.
   *
   * <p>Note: Price is provided by PricingService. Stock is provided by InventoryService.
   */
  public record ProductInfo(ProductId productId, String name, String sku, String imageUrl) {}

  /**
   * Result of creating a product through the API.
   *
   * @param productId the generated product ID
   */
  public record CreatedProduct(ProductId productId) {}

  /**
   * Creates a new product in the catalog.
   *
   * @param sku the unique stock keeping unit
   * @param name the product name
   * @param description the product description
   * @param imageUrl the product image URL
   * @param priceAmount the price amount
   * @param priceCurrency the price currency code (e.g., "EUR")
   * @param category the product category
   * @param stockQuantity the initial stock quantity
   * @return the created product's ID
   */
  public CreatedProduct createProduct(
      String sku,
      String name,
      String description,
      String imageUrl,
      BigDecimal priceAmount,
      String priceCurrency,
      String category,
      int stockQuantity) {
    CreateProductResult result =
        createProductInputPort.execute(
            new CreateProductCommand(
                sku,
                name,
                description,
                imageUrl,
                priceAmount,
                priceCurrency,
                category,
                stockQuantity));
    return new CreatedProduct(ProductId.of(result.productId()));
  }

  /**
   * Retrieves product information by ID.
   *
   * @param productId the product ID
   * @return product info if found
   */
  public Optional<ProductInfo> getProductInfo(ProductId productId) {
    GetProductByIdResult response =
        getProductByIdInputPort.execute(new GetProductByIdQuery(productId.value()));

    if (!response.found() || response.product() == null) {
      return Optional.empty();
    }

    var product = response.product();
    return Optional.of(
        new ProductInfo(product.productId(), product.name(), product.sku(), product.imageUrl()));
  }

  /**
   * Retrieves all products in the catalog.
   *
   * @return list of all products with their information
   */
  public List<ProductInfo> getAllProducts() {
    var result = getAllProductsInputPort.execute(new GetAllProductsQuery());

    return result.products().stream()
        .map(
            product ->
                new ProductInfo(
                    product.productId(), product.name(), product.sku(), product.imageUrl()))
        .toList();
  }
}
