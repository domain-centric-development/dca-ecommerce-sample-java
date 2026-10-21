package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import java.math.BigDecimal;

public record CartItemDto(
    String itemId, String productId, int quantity, BigDecimal price, String currency) {}
