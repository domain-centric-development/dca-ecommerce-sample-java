package dev.domaincentric.sample.ecommerce.product.adapter.outgoing.persistence;

import dev.domaincentric.sample.ecommerce.product.application.shared.ProductRepository;
import dev.domaincentric.sample.ecommerce.product.domain.model.Category;
import dev.domaincentric.sample.ecommerce.product.domain.model.Product;
import dev.domaincentric.sample.ecommerce.product.domain.model.SKU;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory fake of {@link ProductRepository} for tests without a database.
 *
 * <p>It stores and hands out copies, so a change survives only through {@code save}, claims a SKU
 * the way the unique constraint does, and lists by product name (ordinal) as the port promises.
 */
public class InMemoryProductRepository implements ProductRepository {

  private static final Comparator<Product> BY_NAME =
      Comparator.comparing(product -> product.name().value());

  private final ConcurrentHashMap<ProductId, Product> products = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<SKU, ProductId> skuIndex = new ConcurrentHashMap<>();

  @Override
  public Optional<Product> findById(final ProductId id) {
    return Optional.ofNullable(products.get(id)).map(InMemoryProductRepository::copyOf);
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
        .map(InMemoryProductRepository::copyOf)
        .toList();
  }

  @Override
  public List<Product> findAll() {
    return products.values().stream()
        .sorted(BY_NAME)
        .map(InMemoryProductRepository::copyOf)
        .toList();
  }

  /**
   * @throws IllegalStateException if another product already carries this SKU — the answer a unique
   *     index gives, so a caller written against this adapter works unchanged against a relational
   *     one
   */
  @Override
  public Product save(final Product product) {
    final ProductId claimed = skuIndex.putIfAbsent(product.sku(), product.id());
    if (claimed != null && !claimed.equals(product.id())) {
      throw new IllegalStateException("SKU " + product.sku().value() + " is already taken");
    }

    // A product that changed its SKU must stop resolving under the old one
    skuIndex
        .entrySet()
        .removeIf(e -> e.getValue().equals(product.id()) && !e.getKey().equals(product.sku()));

    products.put(product.id(), copyOf(product));
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

  private static Product copyOf(final Product product) {
    return Product.reconstitute(
        product.id(),
        product.sku(),
        product.name(),
        product.description(),
        product.category(),
        product.imageUrl());
  }
}
