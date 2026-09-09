package dev.domaincentric.sample.ecommerce.pricing.domain.event;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainEvent;
import dev.domaincentric.sample.ecommerce.pricing.domain.model.PriceId;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Price;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.UUID;

/** Domain Event indicating that a new price was created for a product. */
public record PriceCreated(
    UUID eventId,
    PriceId priceId,
    ProductId productId,
    Price price,
    Instant effectiveFrom,
    Instant occurredOn)
    implements DomainEvent {

  public static PriceCreated now(
      final PriceId priceId,
      final ProductId productId,
      final Price price,
      final Instant effectiveFrom) {
    return new PriceCreated(
        UUID.randomUUID(), priceId, productId, price, effectiveFrom, Instant.now());
  }
}
