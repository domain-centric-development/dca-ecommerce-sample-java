package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts.GetAllCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming between domain/use case models and Cart DTOs.
 *
 * <p>This converter supports both:
 *
 * <ul>
 *   <li>Domain entities (ShoppingCart) - for legacy code
 *   <li>Use case outputs - for Clean Architecture pattern
 * </ul>
 */
@Component
public final class ShoppingCartDtoConverter {

  /**
   * Converts domain ShoppingCart entity to DTO.
   *
   * @param cart the domain cart
   * @return cart DTO
   * @deprecated Use use case output converters instead
   */
  @Deprecated
  public ShoppingCartDto toDto(final ShoppingCart cart) {
    final List<CartItemDto> itemDtos = cart.items().stream().map(this::toItemDto).toList();

    final Money total = cart.calculateTotal();

    return new ShoppingCartDto(
        cart.id().value(),
        cart.customerId().value(),
        itemDtos,
        cart.status().name(),
        total.amount(),
        total.currency().getCurrencyCode(),
        cart.itemCount());
  }

  /** Converts CreateCartResult to DTO. */
  public ShoppingCartDto toDto(final CreateCartResult output) {
    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        List.of(), // New cart has no items
        output.status(),
        null,
        null,
        0);
  }

  /** Converts EnrichedCart read model to DTO. */
  public ShoppingCartDto toDto(final EnrichedCart cart) {
    final List<CartItemDto> items = cart.items().stream().map(this::toItemDto).toList();

    final Money total = cart.calculateCurrentSubtotal();

    return new ShoppingCartDto(
        cart.cartId().value(),
        cart.customerId().value(),
        items,
        cart.status().name(),
        total.amount(),
        total.currency().getCurrencyCode(),
        items.size());
  }

  /** Converts AddItemToCartResult to DTO. */
  public ShoppingCartDto toDto(final AddItemToCartResult output) {
    final List<CartItemDto> items = output.items().stream().map(this::toItemDto).toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "ACTIVE", // Always active when adding items
        output.totalAmount(),
        output.totalCurrency(),
        items.size());
  }

  /** Converts RemoveItemFromCartResult to DTO. */
  public ShoppingCartDto toDto(final RemoveItemFromCartResult output) {
    final List<CartItemDto> items = output.items().stream().map(this::toItemDto).toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "ACTIVE", // Always active when removing items
        output.totalAmount(),
        output.totalCurrency(),
        items.size());
  }

  /** Converts CheckoutCartResult to DTO. */
  public ShoppingCartDto toDto(final CheckoutCartResult output) {
    final List<CartItemDto> items = output.items().stream().map(this::toItemDto).toList();

    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        items,
        "CHECKED_OUT",
        output.totalAmount(),
        output.totalCurrency(),
        items.size());
  }

  /** Converts GetAllCartsResult to list DTO. */
  public ShoppingCartListDto toListDto(final GetAllCartsResult output) {
    final List<ShoppingCartListDto.CartSummaryDto> summaries =
        output.carts().stream()
            .map(
                cart ->
                    new ShoppingCartListDto.CartSummaryDto(
                        cart.cartId(),
                        cart.customerId(),
                        cart.status(),
                        cart.itemCount(),
                        cart.totalAmount(),
                        cart.totalCurrency()))
            .toList();

    return new ShoppingCartListDto(summaries);
  }

  private CartItemDto toItemDto(final CartItem item) {
    return new CartItemDto(
        item.id().value(),
        item.productId().value(),
        item.quantity().value(),
        item.priceAtAddition().value().amount(),
        item.priceAtAddition().value().currency().getCurrencyCode());
  }

  private CartItemDto toItemDto(final EnrichedCartItem item) {
    // Use current price from article data
    final Money currentPrice = item.currentArticle().currentPrice();
    return new CartItemDto(
        item.cartItemId().value(),
        item.productId().value(),
        item.quantity().value(),
        currentPrice.amount(),
        currentPrice.currency().getCurrencyCode());
  }

  private CartItemDto toItemDto(final AddItemToCartResult.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency());
  }

  private CartItemDto toItemDto(final RemoveItemFromCartResult.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency());
  }

  private CartItemDto toItemDto(final CheckoutCartResult.CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPriceAmount(),
        item.unitPriceCurrency());
  }
}
