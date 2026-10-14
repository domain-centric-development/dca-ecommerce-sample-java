package dev.domaincentric.sample.ecommerce.cart.events;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEvent;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.IntegrationEventType;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when cart contents change.
 *
 * <p>Consolidates multiple internal cart domain events (CartItemAddedToCart,
 * ProductRemovedFromCart, CartItemQuantityChanged, CartCleared) into a single cross-module event.
 * The Checkout context consumes this event to sync checkout sessions instead of listening to
 * individual domain events.
 */
@IntegrationEventType(name = "cart-contents-changed", version = 1)
public record CartContentsChangedEvent(
    UUID eventId, UUID cartId, ChangeType changeType, Instant occurredOn)
    implements IntegrationEvent {

  /**
   * The type of change that occurred in the cart. Each value spells the code it is published as, so
   * renaming a value never changes what a consumer reads.
   */
  public enum ChangeType {
    ITEM_ADDED("ITEM_ADDED"),
    ITEM_REMOVED("ITEM_REMOVED"),
    QUANTITY_CHANGED("QUANTITY_CHANGED"),
    CART_CLEARED("CART_CLEARED");

    private final String code;

    ChangeType(final String code) {
      this.code = code;
    }

    /**
     * The spelling this value is published as.
     *
     * @return the code
     */
    public String code() {
      return code;
    }
  }

  /** Creates a new event with the given cart ID and change type. */
  public static CartContentsChangedEvent now(UUID cartId, ChangeType changeType) {
    return new CartContentsChangedEvent(UUID.randomUUID(), cartId, changeType, Instant.now());
  }
}
