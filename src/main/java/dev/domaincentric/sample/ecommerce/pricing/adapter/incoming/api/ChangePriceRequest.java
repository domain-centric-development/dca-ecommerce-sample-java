package dev.domaincentric.sample.ecommerce.pricing.adapter.incoming.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * The body of a price change: the new amount, in the currency of the price it replaces.
 *
 * @param price the new amount, greater than zero
 */
public record ChangePriceRequest(@NotNull @Positive BigDecimal price) {}
