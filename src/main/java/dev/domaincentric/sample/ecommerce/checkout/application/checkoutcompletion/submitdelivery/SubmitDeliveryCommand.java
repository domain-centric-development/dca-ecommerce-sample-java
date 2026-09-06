package dev.domaincentric.sample.ecommerce.checkout.application.submitdelivery;

import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * Input model for submitting delivery information during checkout.
 *
 * @param sessionId the checkout session ID
 * @param street the street address
 * @param streetLine2 optional second address line
 * @param city the city
 * @param postalCode the postal code
 * @param country the country
 * @param state optional state/province
 * @param shippingOptionId the selected shipping option ID
 * @param shippingOptionName the shipping option display name
 * @param estimatedDelivery the estimated delivery time
 * @param shippingCost the shipping cost
 * @param currencyCode the currency code for shipping cost
 */
public record SubmitDeliveryCommand(
    String sessionId,
    String street,
    @Nullable String streetLine2,
    String city,
    String postalCode,
    String country,
    @Nullable String state,
    String shippingOptionId,
    String shippingOptionName,
    String estimatedDelivery,
    BigDecimal shippingCost,
    String currencyCode) {

  /** Compact constructor with validation. */
  public SubmitDeliveryCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (street == null || street.isBlank()) {
      throw new IllegalArgumentException("Street cannot be null or blank");
    }
    if (city == null || city.isBlank()) {
      throw new IllegalArgumentException("City cannot be null or blank");
    }
    if (postalCode == null || postalCode.isBlank()) {
      throw new IllegalArgumentException("Postal code cannot be null or blank");
    }
    if (country == null || country.isBlank()) {
      throw new IllegalArgumentException("Country cannot be null or blank");
    }
    if (shippingOptionId == null || shippingOptionId.isBlank()) {
      throw new IllegalArgumentException("Shipping option ID cannot be null or blank");
    }
    if (shippingOptionName == null || shippingOptionName.isBlank()) {
      throw new IllegalArgumentException("Shipping option name cannot be null or blank");
    }
    if (estimatedDelivery == null || estimatedDelivery.isBlank()) {
      throw new IllegalArgumentException("Estimated delivery cannot be null or blank");
    }
    if (shippingCost == null) {
      throw new IllegalArgumentException("Shipping cost cannot be null");
    }
    if (currencyCode == null || currencyCode.isBlank()) {
      throw new IllegalArgumentException("Currency code cannot be null or blank");
    }
  }
}
