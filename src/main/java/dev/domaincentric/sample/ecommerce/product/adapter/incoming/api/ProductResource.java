package dev.domaincentric.sample.ecommerce.product.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductCommand;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductInputPort;
import dev.domaincentric.sample.ecommerce.product.application.createproduct.CreateProductResult;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsQuery;
import dev.domaincentric.sample.ecommerce.product.application.getallproducts.GetAllProductsResult;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdInputPort;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdQuery;
import dev.domaincentric.sample.ecommerce.product.application.getproductbyid.GetProductByIdResult;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Resource for Product operations.
 *
 * <p>This is a primary adapter (incoming) in Hexagonal Architecture that exposes product
 * functionality via REST API. It depends on use case interfaces (input ports) rather than the use
 * case classes, following the Dependency Inversion Principle.
 *
 * <p><b>Authorization:</b> reading the catalog is public — it is the same assortment the shop pages
 * show. Creating a product is an operator action and requires the staff role. The JWT filter gives
 * <i>every</i> request an authentication, anonymous ones included, so {@code authenticated()} in
 * the security configuration does not guard this; the adapter has to.
 *
 * <p><b>Bearer only:</b> {@code /api/**} is authenticated by an {@code Authorization: Bearer}
 * header and never by a browser cookie, which is what makes its CSRF exemption sound (ADR-035).
 */
@RestController
@RequestMapping("/api/products")
public class ProductResource {

  private final CreateProductInputPort createProduct;
  private final GetAllProductsInputPort getAllProducts;
  private final GetProductByIdInputPort getProductById;
  private final ProductDtoConverter converter;
  private final IdentityProvider identityProvider;

  public ProductResource(
      final CreateProductInputPort createProduct,
      final GetAllProductsInputPort getAllProducts,
      final GetProductByIdInputPort getProductById,
      final ProductDtoConverter converter,
      final IdentityProvider identityProvider) {
    this.createProduct = createProduct;
    this.getAllProducts = getAllProducts;
    this.getProductById = getProductById;
    this.converter = converter;
    this.identityProvider = identityProvider;
  }

  @PostMapping
  public ResponseEntity<ProductDto> createProduct(
      @Valid @RequestBody final CreateProductRequest request) {

    if (!identityProvider.getCurrentIdentity().hasRole(IdentityProvider.Identity.ROLE_STAFF)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    final CreateProductCommand input =
        new CreateProductCommand(
            request.sku(),
            request.name(),
            request.description() != null ? request.description() : "",
            request.imageUrl() != null ? request.imageUrl() : "",
            request.price(),
            "EUR", // Default currency
            request.category(),
            request.stock());

    try {
      final CreateProductResult output = createProduct.execute(input);
      return ResponseEntity.status(HttpStatus.CREATED).body(converter.toDto(output));
    } catch (final IllegalArgumentException | IllegalStateException e) {
      // A malformed SKU or a duplicate one is the caller's mistake, not the server's.
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping
  public ResponseEntity<List<ProductDto>> getAllProducts() {
    final GetAllProductsResult output = getAllProducts.execute(new GetAllProductsQuery());

    final List<ProductDto> products = output.products().stream().map(converter::toDto).toList();

    return ResponseEntity.ok(products);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDto> getProductById(@PathVariable final String id) {
    final GetProductByIdResult output = getProductById.execute(new GetProductByIdQuery(id));

    if (!output.found()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(converter.toDto(output));
  }
}
