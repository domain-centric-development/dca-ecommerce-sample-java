package dev.domaincentric.sample.ecommerce.checkout.domain.model;

import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import java.io.Serial;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * Raised when "Place Order" meets an order that is no longer the one the review showed: a price
 * changed since, or anything else — a change made in another tab counts alike.
 *
 * <p>Nothing is ordered then; the review shows the order as it is today and the customer places it
 * again.
 */
public final class CheckoutChangedSinceReviewException extends DomainException {

  @Serial private static final long serialVersionUID = 1L;

  private final CheckoutSessionId sessionId;
  private final @Nullable String productWithChangedPrice;

  private CheckoutChangedSinceReviewException(
      final CheckoutSessionId sessionId, final @Nullable String productWithChangedPrice) {
    super(
        productWithChangedPrice == null
            ? "Checkout " + sessionId.value() + " has changed since its review"
            : "The price of '"
                + productWithChangedPrice
                + "' in checkout "
                + sessionId.value()
                + " has changed since its review");
    this.sessionId = sessionId;
    this.productWithChangedPrice = productWithChangedPrice;
  }

  /**
   * The refusal for a line whose price changed since the review.
   *
   * @param sessionId the checkout
   * @param productName the name of the line's product, as the line shows it
   * @return the refusal
   */
  public static CheckoutChangedSinceReviewException priceChanged(
      final CheckoutSessionId sessionId, final String productName) {
    return new CheckoutChangedSinceReviewException(sessionId, productName);
  }

  /**
   * The refusal for any other change since the review.
   *
   * @param sessionId the checkout
   * @return the refusal
   */
  public static CheckoutChangedSinceReviewException orderChanged(
      final CheckoutSessionId sessionId) {
    return new CheckoutChangedSinceReviewException(sessionId, null);
  }

  public CheckoutSessionId sessionId() {
    return sessionId;
  }

  /** The product whose price changed, or empty when something else changed. */
  public Optional<String> productWithChangedPrice() {
    return Optional.ofNullable(productWithChangedPrice);
  }
}
