package dev.domaincentric.sample.ecommerce.product.adapter.incoming.mcp;

import dev.domaincentric.sample.ecommerce.product.adapter.incoming.api.ProductDto;
import dev.domaincentric.sample.ecommerce.product.adapter.incoming.api.ProductDtoConverter;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsResult;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdQuery;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import java.util.List;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

/**
 * MCP Tools for Product Catalog operations.
 *
 * <p>This class exposes product catalog functionality as MCP tools that can be invoked by AI models
 * through the Model Context Protocol. These tools provide read-only access to the product catalog,
 * allowing AI assistants to query products and retrieve product details.
 *
 * <p><b>Primary Adapter:</b> This is a primary (incoming) adapter in the Hexagonal Architecture
 * pattern, exposing domain functionality through the MCP protocol. It uses Clean Architecture use
 * cases (input ports) to access domain logic.
 *
 * <p><b>Clean Architecture:</b> This adapter depends on use case interfaces (input ports) rather
 * than the use case classes, following the Dependency Inversion Principle.
 *
 * <p><b>Available Tools:</b>
 *
 * <ul>
 *   <li>{@link #getAllProducts()} - Retrieve all products in the catalog
 *   <li>{@link #getProductById(String)} - Get detailed product information by ID
 * </ul>
 */
@Component
public class ProductCatalogMcpToolProvider {

  private final GetAllProductsInputPort getAllProductsUseCase;
  private final GetProductByIdInputPort getProductByIdUseCase;
  private final ProductDtoConverter productDtoConverter;

  public ProductCatalogMcpToolProvider(
      final GetAllProductsInputPort getAllProductsUseCase,
      final GetProductByIdInputPort getProductByIdUseCase,
      final ProductDtoConverter productDtoConverter) {
    this.getAllProductsUseCase = getAllProductsUseCase;
    this.getProductByIdUseCase = getProductByIdUseCase;
    this.productDtoConverter = productDtoConverter;
  }

  /**
   * Retrieves all products in the catalog.
   *
   * <p>This tool returns a complete list of all products available in the e-commerce catalog,
   * including their SKU, name, price, category, and stock information.
   *
   * @return list of all products as DTOs
   */
  @McpTool(
      name = "all-products",
      description =
          "Get all products in the catalog. Returns complete product information including SKU, name, price, category, and available stock.")
  public List<ProductDto> getAllProducts() {
    final GetAllProductsResult output = getAllProductsUseCase.execute(new GetAllProductsQuery());

    return output.products().stream().map(productDtoConverter::toDto).toList();
  }

  /**
   * Gets detailed product information by product ID.
   *
   * <p>This tool retrieves complete product details using the internal product ID. Useful for
   * getting detailed information about a specific product when the ID is known.
   *
   * @param id the product ID (UUID format)
   * @return product information if found, or null if not found
   * @throws IllegalArgumentException if ID format is invalid
   */
  @McpTool(
      name = "product-by-id",
      description =
          "Get detailed product information by product ID. Requires the internal product UUID. Returns complete product details including all attributes.")
  public ProductDto getProductById(final String id) {
    return switch (getProductByIdUseCase.execute(new GetProductByIdQuery(id))) {
      case GetProductByIdResult.Found found -> productDtoConverter.toDto(found.product());
      case GetProductByIdResult.ProductNotFound refusal -> null;
    };
  }
}
