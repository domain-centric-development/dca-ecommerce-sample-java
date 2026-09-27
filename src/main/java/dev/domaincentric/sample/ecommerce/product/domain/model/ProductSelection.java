package dev.domaincentric.sample.ecommerce.product.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.Value;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Up to {@link #MAX_SIZE} different catalogue products that have a price, drawn at random.
 *
 * @param productIds the drawn products, in draw order
 */
public record ProductSelection(List<ProductId> productIds) implements Value {

  /** The most products a selection holds. */
  public static final int MAX_SIZE = 8;

  public ProductSelection {
    if (productIds == null) {
      throw new IllegalArgumentException("Product IDs cannot be null");
    }
    productIds = List.copyOf(productIds);
    if (productIds.size() > MAX_SIZE) {
      throw new IllegalArgumentException("A selection holds at most " + MAX_SIZE + " products");
    }
  }

  /**
   * Draws up to {@link #MAX_SIZE} different products from the products that have a price: every
   * candidate when there are fewer, none when there are none. A candidate named twice counts once.
   * The draw depends only on the candidates and the random source.
   *
   * @param pricedProducts the candidates
   * @param random the source of randomness
   * @return the selection
   */
  public static ProductSelection draw(
      final Collection<ProductId> pricedProducts, final RandomGenerator random) {
    if (pricedProducts == null || random == null) {
      throw new IllegalArgumentException("Candidates and random source cannot be null");
    }
    final List<ProductId> pool = new ArrayList<>(new LinkedHashSet<>(pricedProducts));
    final int size = Math.min(MAX_SIZE, pool.size());
    for (int i = 0; i < size; i++) {
      final int pick = random.nextInt(i, pool.size());
      final ProductId drawn = pool.get(pick);
      pool.set(pick, pool.get(i));
      pool.set(i, drawn);
    }
    return new ProductSelection(pool.subList(0, size));
  }
}
