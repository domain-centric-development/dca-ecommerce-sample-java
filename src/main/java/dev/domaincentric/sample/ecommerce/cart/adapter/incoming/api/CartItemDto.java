package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import org.jspecify.annotations.Nullable;

/**
 * One line of a cart as the API answers it.
 *
 * @param price today's unit price; left out for a line whose product the catalogue no longer knows
 * @param currency the price's currency code; left out with the price
 * @param available whether the line can be sold today
 */
public record CartItemDto(
    String itemId,
    String productId,
    int quantity,
    @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable BigDecimal price,
    @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable String currency,
    boolean available) {}
