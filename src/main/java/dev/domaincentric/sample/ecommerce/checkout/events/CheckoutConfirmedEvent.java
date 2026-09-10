package dev.domaincentric.sample.ecommerce.checkout.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEventType;
import dev.domaincentric.sample.ecommerce.cart.events.CartCompletionTrigger;
import dev.domaincentric.sample.ecommerce.inventory.events.StockReductionTrigger;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Integration Event published when a checkout is confirmed.
 *
 * <p>This event is published for cross-module consumption. Internal domain event {@code
 * CheckoutConfirmed} is converted to this integration event by {@code
 * CheckoutConfirmedEventPublisher}.
 *
 * <p>Implements consumer-defined trigger interfaces (Interface Inversion pattern) so that consuming
 * modules listen to their own interfaces and avoid depending on the Checkout module:
 *
 * <ul>
 *   <li>{@link CartCompletionTrigger} — triggers cart completion in the Cart module
 *   <li>{@link StockReductionTrigger} — triggers stock reduction in the Inventory module
 * </ul>
 */
@IntegrationEventType(name = "checkout-confirmed", version = 2)
public record CheckoutConfirmedEvent(
    UUID eventId,
    String sessionId,
    String cartId,
    String customerId,
    Money totalAmount,
    List<LineItemInfo> items,
    Instant occurredOn)
    implements IntegrationEvent, CartCompletionTrigger, StockReductionTrigger {

  /**
   * Lightweight DTO for line item information.
   *
   * @param productId the product ID from Shared Kernel
   * @param quantity the quantity
   */
  public record LineItemInfo(ProductId productId, int quantity, String positionSnapshot) {}

  @Override
  public List<String> purchasedPositions() {
    return items.stream().map(LineItemInfo::positionSnapshot).toList();
  }

  @Override
  public List<OrderLineItem> orderLineItems() {
    return items.stream()
        .map(item -> new OrderLineItem(item.productId(), item.quantity()))
        .toList();
  }
}
