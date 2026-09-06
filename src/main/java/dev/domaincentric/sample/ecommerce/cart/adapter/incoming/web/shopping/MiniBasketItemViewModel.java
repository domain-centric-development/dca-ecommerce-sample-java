package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

/**
 * ViewModel for a single item in the mini basket dropdown.
 *
 * <p>Used by {@link MiniBasketControllerAdvice} to provide mini basket data to all Pug templates
 * via the layout.
 *
 * @param name the product name
 * @param quantity the item quantity
 * @param price the formatted line total (e.g., "29.98 EUR")
 */
public record MiniBasketItemViewModel(String name, int quantity, String price) {}
