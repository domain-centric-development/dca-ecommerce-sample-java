package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Output model for changing a product's price.
 *
 * @param productId the product whose price changed
 * @param priceAmount the price's amount now
 * @param priceCurrency the price's currency code
 * @param effectiveFrom when the new price became effective
 */
public record ChangePriceResult(
    String productId, BigDecimal priceAmount, String priceCurrency, Instant effectiveFrom) {}
