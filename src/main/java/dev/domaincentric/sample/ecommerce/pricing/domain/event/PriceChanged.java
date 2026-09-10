package dev.domaincentric.sample.ecommerce.pricing.domain.event;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.UUID;

/** Domain Event indicating that a product's price was changed. */
public record PriceChanged(
    UUID eventId,
    PriceId priceId,
    ProductId productId,
    Price oldPrice,
    Price newPrice,
    Instant effectiveFrom,
    Instant occurredOn)
    implements DomainEvent {

  public static PriceChanged now(
      final PriceId priceId,
      final ProductId productId,
      final Price oldPrice,
      final Price newPrice,
      final Instant effectiveFrom) {
    return new PriceChanged(
        UUID.randomUUID(), priceId, productId, oldPrice, newPrice, effectiveFrom, Instant.now());
  }
}
