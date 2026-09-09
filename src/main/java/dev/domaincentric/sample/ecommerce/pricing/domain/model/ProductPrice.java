package dev.domaincentric.sample.ecommerce.pricing.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.BaseAggregateRoot;
import dev.domaincentric.sample.ecommerce.pricing.domain.event.PriceChanged;
import dev.domaincentric.sample.ecommerce.pricing.domain.event.PriceCreated;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;

/**
 * ProductPrice Aggregate Root.
 *
 * <p>Represents a product's price in the pricing bounded context. This aggregate manages price
 * changes for products and ensures pricing invariants are maintained.
 *
 * <p><b>Business Rules:</b>
 *
 * <ul>
 *   <li>Price must be greater than zero
 *   <li>Price changes are tracked with effective dates
 * </ul>
 *
 * <p><b>Domain Events:</b>
 *
 * <ul>
 *   <li>{@link PriceCreated} - when a new price is created
 *   <li>{@link PriceChanged} - when the price is updated
 * </ul>
 */
public final class ProductPrice extends BaseAggregateRoot<ProductPrice, PriceId> {

  private final PriceId id;
  private final ProductId productId;
  private Price currentPrice;
  private Instant effectiveFrom;

  private ProductPrice(
      final PriceId id,
      final ProductId productId,
      final Price currentPrice,
      final Instant effectiveFrom) {
    this.id = id;
    this.productId = productId;
    this.currentPrice = currentPrice;
    this.effectiveFrom = effectiveFrom;
  }

  /**
   * Creates a new ProductPrice aggregate.
   *
   * <p>Raises a {@link PriceCreated} domain event.
   *
   * @param productId the product ID
   * @param price the initial price (must be greater than zero)
   * @return the new ProductPrice aggregate
   * @throws IllegalArgumentException if price is not greater than zero
   */
  public static ProductPrice create(final ProductId productId, final Price price) {
    validatePriceGreaterThanZero(price);
    final PriceId priceId = PriceId.generate();
    final Instant effectiveFrom = Instant.now();
    final ProductPrice productPrice = new ProductPrice(priceId, productId, price, effectiveFrom);
    productPrice.registerEvent(PriceCreated.now(priceId, productId, price, effectiveFrom));
    return productPrice;
  }

  @Override
  public PriceId id() {
    return id;
  }

  public ProductId productId() {
    return productId;
  }

  public Price currentPrice() {
    return currentPrice;
  }

  public Instant effectiveFrom() {
    return effectiveFrom;
  }

  /**
   * Updates the price to a new value.
   *
   * <p>Raises a {@link PriceChanged} domain event.
   *
   * @param newPrice the new price (must be greater than zero)
   * @throws IllegalArgumentException if newPrice is not greater than zero
   */
  public void updatePrice(final Price newPrice) {
    validatePriceGreaterThanZero(newPrice);

    final Price oldPrice = this.currentPrice;
    final Instant newEffectiveFrom = Instant.now();

    this.currentPrice = newPrice;
    this.effectiveFrom = newEffectiveFrom;

    registerEvent(PriceChanged.now(this.id, this.productId, oldPrice, newPrice, newEffectiveFrom));
  }

  private static void validatePriceGreaterThanZero(final Price price) {
    if (price == null) throw new IllegalArgumentException("Price is required");
  }
}
