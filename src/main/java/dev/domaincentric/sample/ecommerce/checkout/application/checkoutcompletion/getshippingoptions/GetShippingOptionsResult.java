package dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output model containing available shipping options.
 *
 * @param shippingOptions list of available shipping options
 */
public record GetShippingOptionsResult(List<ShippingOptionData> shippingOptions) {

  /**
   * Individual shipping option details.
   *
   * @param id the shipping option identifier
   * @param name the display name
   * @param estimatedDelivery the estimated delivery timeframe
   * @param cost the shipping cost amount
   * @param currencyCode the currency code (e.g., "EUR")
   */
  public record ShippingOptionData(
      String id, String name, String estimatedDelivery, BigDecimal cost, String currencyCode) {}
}
