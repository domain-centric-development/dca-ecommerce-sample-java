package dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import dev.domaincentric.sample.ecommerce.sharedkernel.infrastructure.AsyncInitialize;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * In-memory implementation of ProductPriceRepository.
 *
 * <p>This secondary adapter provides a thread-safe in-memory storage for product prices using
 * ConcurrentHashMap. A secondary index on ProductId enables efficient lookups by product.
 *
 * <p><b>Initialization:</b> Pricing data is initialized by the SampleDataInitializer, which calls
 * the Pricing API ({@code PricingService.provisionInitialPrice}) after creating each product.
 *
 * <p>In a production system, this would be replaced with a database implementation.
 *
 * @see AsyncInitialize
 */
@Repository
@AsyncInitialize(priority = 60, description = "Initialize pricing data repository")
public class InMemoryProductPriceRepository implements ProductPriceRepository {

  private static final Logger logger =
      LoggerFactory.getLogger(InMemoryProductPriceRepository.class);

  private final ConcurrentHashMap<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, PriceId> productIdIndex = new ConcurrentHashMap<>();

  public InMemoryProductPriceRepository() {}

  @Override
  public Optional<ProductPrice> findById(final PriceId id) {
    return Optional.ofNullable(prices.get(id));
  }

  @Override
  public Optional<ProductPrice> findByProductId(final ProductId productId) {
    final PriceId priceId = productIdIndex.get(productId);
    if (priceId == null) {
      return Optional.empty();
    }
    return findById(priceId);
  }

  @Override
  public List<ProductPrice> findByProductIds(final Collection<ProductId> productIds) {
    return productIds.stream()
        .map(productIdIndex::get)
        .filter(priceId -> priceId != null)
        .map(prices::get)
        .filter(price -> price != null)
        .toList();
  }

  @Override
  public ProductPrice save(final ProductPrice productPrice) {
    prices.put(productPrice.id(), productPrice);
    productIdIndex.put(productPrice.productId(), productPrice.id());
    return productPrice;
  }

  @Override
  public void deleteById(final PriceId id) {
    final ProductPrice productPrice = prices.remove(id);
    if (productPrice != null) {
      productIdIndex.remove(productPrice.productId());
    }
  }

  /**
   * Asynchronous initialization method triggered by {@link AsyncInitialize}.
   *
   * <p>This method performs cache warmup and logs initialization status. Actual pricing data is
   * populated via PricingService.provisionInitialPrice when products are created by
   * SampleDataInitializer.
   *
   * @see AsyncInitialize
   * @see dev.domaincentric.sample.ecommerce.infrastructure.config.AsyncConfiguration
   * @see dev.domaincentric.sample.ecommerce.infrastructure.support.AsyncInitializationProcessor
   */
  @Async
  public void asyncInitialize() {
    logger.info("Starting async initialization of ProductPriceRepository...");

    try {
      // Wait for product events to be processed
      Thread.sleep(3000);

      logger.info(
          "ProductPriceRepository initialization completed. {} price entries available.",
          prices.size());

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.error("ProductPriceRepository async initialization interrupted", e);
    } catch (Exception e) {
      logger.error("Error during ProductPriceRepository async initialization", e);
    }
  }
}
