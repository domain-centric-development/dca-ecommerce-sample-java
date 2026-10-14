package dev.domaincentric.sample.ecommerce.pricing.application.provisioninitialprice;

import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Input model for setting or updating a product's price.
 *
 * <p>{@code eventId} is the id of the integration event that asked for the effect; the use case
 * does not produce the effect a second time for the same id. A caller without an event passes none,
 * and the effect is not deduplicated.
 *
 * @param productId the product ID
 * @param priceAmount the price amount (must be greater than zero)
 * @param priceCurrency the price currency code (e.g., "EUR", "USD")
 * @param eventId the id of the integration event that asked for it, if any
 */
public record ProvisionInitialPriceCommand(
    String productId, BigDecimal priceAmount, String priceCurrency, @Nullable UUID eventId) {

  /** A price no integration event asked for — not deduplicated. */
  public ProvisionInitialPriceCommand(
      final String productId, final BigDecimal priceAmount, final String priceCurrency) {
    this(productId, priceAmount, priceCurrency, null);
  }

  /** Compact constructor with validation. */
  public ProvisionInitialPriceCommand {
    if (productId == null || productId.isBlank()) {
      throw new IllegalArgumentException("Product ID cannot be null or blank");
    }
    if (priceAmount == null) {
      throw new IllegalArgumentException("Price amount cannot be null");
    }
    if (priceAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Price amount must be greater than zero");
    }
    if (priceCurrency == null || priceCurrency.isBlank()) {
      throw new IllegalArgumentException("Price currency cannot be null or blank");
    }
  }
}
