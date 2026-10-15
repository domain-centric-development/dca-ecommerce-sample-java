package dev.domaincentric.sample.ecommerce.pricing.application.changeprice;

import java.math.BigDecimal;

/**
 * Input model for changing a product's price.
 *
 * <p>The new price carries no currency: it keeps the currency of the price it replaces.
 *
 * @param productId the product whose price changes
 * @param priceAmount the new amount
 */
public record ChangePriceCommand(String productId, BigDecimal priceAmount) {}
