package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Input model for completing a shopping cart after checkout confirmation.
 *
 * <p>{@code eventId} is the id of the integration event that asked for the effect; the use case
 * does not produce the effect a second time for the same id. A caller without an event passes none,
 * and the effect is not deduplicated.
 *
 * @param cartId the cart ID to complete
 * @param sessionId the confirmed checkout session
 * @param purchasedPositions the purchased positions, as {@code <cart item id>:<units>} snapshots
 * @param eventId the id of the integration event that asked for it, if any
 */
public record CompleteCartCommand(
    String cartId,
    String sessionId,
    java.util.List<String> purchasedPositions,
    @Nullable UUID eventId) {

  /** A completion no integration event asked for — not deduplicated. */
  public CompleteCartCommand(
      final String cartId,
      final String sessionId,
      final java.util.List<String> purchasedPositions) {
    this(cartId, sessionId, purchasedPositions, null);
  }

  /** Compact constructor with validation. */
  public CompleteCartCommand {
    purchasedPositions = java.util.List.copyOf(purchasedPositions);
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
  }
}
