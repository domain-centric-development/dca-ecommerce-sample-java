package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import dev.domaincentric.sample.ecommerce.sharedkernel.infrastructure.AsyncInitialize;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ProductRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for products using
 * ConcurrentHashMap. In a production system, this would be replaced with a database implementation.
 *
 * <p>Listings are ordered by product name (ordinal), as the {@link ProductRepository} contract
 * requires — a hash map has no order of its own, and the catalog page must read the same in every
 * persistence profile.
 *
 * <p><b>Async Initialization:</b> This repository uses {@link AsyncInitialize} to perform
 * non-blocking cache warmup. The {@code asyncInitialize()} method is invoked asynchronously after
 * bean initialization, allowing the application to start without waiting for cache warmup.
 *
 * @see AsyncInitialize
 */
@Repository
@AsyncInitialize(priority = 50, description = "Warm up product cache")
public class InMemoryProductRepository implements ProductRepository {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryProductRepository.class);

  private static final Comparator<Product> BY_NAME =
      Comparator.comparing(product -> product.name().value());

  private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<SKU, ProductId> skuIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<Product> findById(final ProductId id) {
    return Optional.ofNullable(products.get(id));
  }

  @Override
  public Optional<Product> findBySku(final SKU sku) {
    final ProductId productId = skuIndex.get(sku);
    if (productId == null) {
      return Optional.empty();
    }
    return findById(productId);
  }

  @Override
  public List<Product> findByCategory(final Category category) {
    return products.values().stream()
        .filter(product -> product.category().equals(category))
        .sorted(BY_NAME)
        .toList();
  }

  @Override
  public List<Product> findAll() {
    return products.values().stream().sorted(BY_NAME).toList();
  }

  @Override
  public Product save(final Product product) {
    products.put(product.id(), product);
    skuIndex.put(product.sku(), product.id());
    return product;
  }

  @Override
  public void deleteById(final ProductId id) {
    final Product product = products.remove(id);
    if (product != null) {
      skuIndex.remove(product.sku());
    }
  }

  @Override
  public boolean existsBySku(final SKU sku) {
    return skuIndex.containsKey(sku);
  }

  /**
   * Asynchronous initialization method triggered by {@link AsyncInitialize}.
   *
   * <p>This method is invoked after bean construction to warm up the product cache in the
   * background. The {@code @Async} annotation ensures non-blocking execution, allowing the
   * application to start immediately without waiting for cache warmup.
   *
   * <p><b>Pattern:</b> This demonstrates the {@code @AsyncInitialize} pattern where heavy
   * initialization work is offloaded to a background thread, improving application startup time.
   *
   * @see AsyncInitialize
   * @see dev.domaincentric.sample.ecommerce.infrastructure.config.AsyncConfiguration
   * @see dev.domaincentric.sample.ecommerce.infrastructure.support.AsyncInitializationProcessor
   */
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductRepository cache...");

    try {
      // Simulate cache warmup delay
      Thread.sleep(2000);

      // In a real application, this would:
      // - Preload frequently accessed products
      // - Build search indexes
      // - Warm up caches
      int productCount = products.size();

      logger.info(
          "ProductRepository cache warmup completed. Initialized {} products.", productCount);

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ProductRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ProductRepository async initialization", e);
    }
  }
}
