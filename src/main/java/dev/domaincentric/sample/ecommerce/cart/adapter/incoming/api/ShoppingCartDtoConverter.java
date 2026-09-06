package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.checkoutcart.CheckoutCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts.GetAllCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming use case results and the enriched cart read model into Cart DTOs.
 *
 * <p>Results deliver values ({@link Money}, {@link CartItemSummary}); this converter splits them
 * into the amount and currency-code fields of the DTOs. It derives nothing itself.
 */
@Component
public final class ShoppingCartDtoConverter {

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
        output.total().amount(),
        output.total().currency().getCurrencyCode(),
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
        output.total().amount(),
        output.total().currency().getCurrencyCode(),
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
        output.total().amount(),
        output.total().currency().getCurrencyCode(),
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
                        cart.total().amount(),
                        cart.total().currency().getCurrencyCode()))
            .toList();

    return new ShoppingCartListDto(summaries);
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

  private CartItemDto toItemDto(final CartItemSummary item) {
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        item.unitPrice().amount(),
        item.unitPrice().currency().getCurrencyCode());
  }
}
