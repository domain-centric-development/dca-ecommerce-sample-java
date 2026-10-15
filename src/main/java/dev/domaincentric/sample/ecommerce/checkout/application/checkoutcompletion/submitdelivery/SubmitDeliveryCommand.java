package dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitdelivery;

import org.jspecify.annotations.Nullable;

/**
 * Input model for submitting delivery information during checkout.
 *
 * @param sessionId the checkout session ID
 * @param customerId the customer the caller is acting as
 * @param street the street address
 * @param streetLine2 optional second address line
 * @param city the city
 * @param postalCode the postal code
 * @param country the country
 * @param state optional state/province
 * @param shippingOptionId the selected shipping option ID; name, estimate and price are the shop's
 */
public record SubmitDeliveryCommand(
    String sessionId,
    String customerId,
    String street,
    @Nullable String streetLine2,
    String city,
    String postalCode,
    String country,
    @Nullable String state,
    String shippingOptionId) {

  /** Compact constructor with validation. */
  public SubmitDeliveryCommand {
    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("Session ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
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
  }
}
