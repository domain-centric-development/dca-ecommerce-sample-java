package dev.domaincentric.sample.ecommerce.pricing.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.pricing.application.shared.ProductPriceRepository;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.ProductPrice;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fake of {@link ProductPriceRepository} for tests without a database: one price per
 * product, stored and handed out as copies, so a change survives only through {@code save}.
 */
public class InMemoryProductPriceRepository implements ProductPriceRepository {

  private final ConcurrentHashMap<PriceId, ProductPrice> prices = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<ProductId, PriceId> productIdIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<ProductPrice> findById(final PriceId id) {
    return Optional.ofNullable(prices.get(id)).map(InMemoryProductPriceRepository::copyOf);
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
        .map(InMemoryProductPriceRepository::copyOf)
        .toList();
  }

  @Override
  public ProductPrice save(final ProductPrice productPrice) {
    prices.put(productPrice.id(), copyOf(productPrice));
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

  private static ProductPrice copyOf(final ProductPrice price) {
    return ProductPrice.reconstitute(
        price.id(), price.productId(), price.currentPrice(), price.effectiveFrom());
  }
}
