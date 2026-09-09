package dev.domaincentric.sample.ecommerce.product.application.createproduct;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.ImageUrl;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductDescription;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductFactory;
import dev.domaincentric.sample.ecommerce.product.domain.model.ProductName;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import java.util.Currency;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new product.
 *
 * <p>This use case orchestrates the creation of a new product by:
 *
 * <ol>
 *   <li>Validating business rules (SKU uniqueness)
 *   <li>Creating the product aggregate using the factory
 *   <li>Persisting the product via repository
 *   <li>Publishing domain events (for Pricing and Inventory contexts)
 * </ol>
 *
 * <p><b>Note:</b> The product aggregate only stores identity and description data. The initial
 * price and stock are included in the ProductCreated event for synchronization with the Pricing and
 * Inventory bounded contexts respectively.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CreateProductInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class CreateProductUseCase implements CreateProductInputPort {

  private final ProductRepository productRepository;
  private final ProductFactory productFactory;
  private final DomainEventPublisher eventPublisher;

  public CreateProductUseCase(
      final ProductRepository productRepository,
      final ProductFactory productFactory,
      final DomainEventPublisher eventPublisher) {
    this.productRepository = productRepository;
    this.productFactory = productFactory;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public CreateProductResult execute(final CreateProductCommand input) {
    final SKU sku = new SKU(input.sku());

    // Business rule: SKU must be unique
    if (productRepository.existsBySku(sku)) {
      throw new IllegalArgumentException("Product with SKU already exists: " + sku.value());
    }

    // Convert input to domain value objects
    final ProductName name = new ProductName(input.name());
    final ProductDescription description = new ProductDescription(input.description());
    final ImageUrl imageUrl = new ImageUrl(input.imageUrl());
    final Price initialPrice =
        Price.of(Money.of(input.priceAmount(), Currency.getInstance(input.priceCurrency())));
    final Category category = new Category(input.category());

    // Create product aggregate (initial price and stock included in ProductCreated event)
    final Product product =
        productFactory.createProduct(
            sku, name, description, category, imageUrl, initialPrice, input.stockQuantity());

    // Persist
    productRepository.save(product);

    // Publish domain events (ProductCreated event will trigger Pricing and Inventory contexts)
    eventPublisher.publishAndClearEvents(product);

    // Map to output (price comes from input since it's not stored in Product)
    return new CreateProductResult(
        product.id().value().toString(),
        product.sku().value(),
        product.name().value(),
        product.description().value(),
        product.imageUrl().value(),
        input.priceAmount(),
        input.priceCurrency(),
        product.category().name());
  }
}
