package dev.domaincentric.sample.ecommerce.pricing.application.setproductprice;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Output model for setting or updating a product's price.
 *
 * @param priceId the unique identifier of the price record
 * @param productId the product ID
 * @param priceAmount the current price amount
 * @param priceCurrency the price currency code
 * @param effectiveFrom when the price became effective
 * @param created true if a new price was created, false if an existing price was updated
 * @param alreadyApplied whether the event had set this price before, so nothing changed
 */
public record SetProductPriceResult(
    String priceId,
    String productId,
    BigDecimal priceAmount,
    String priceCurrency,
    Instant effectiveFrom,
    boolean created,
    boolean alreadyApplied) {}
