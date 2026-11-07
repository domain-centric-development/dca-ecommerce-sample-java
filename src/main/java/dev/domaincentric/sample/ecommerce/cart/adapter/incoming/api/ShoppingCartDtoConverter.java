package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.api;

import dev.domaincentric.sample.ecommerce.cart.application.cartoversight.getallcarts.GetAllCartsResult;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.additemtocart.AddItemToCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.removeitemfromcart.RemoveItemFromCartResult;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCart;
import dev.domaincentric.sample.ecommerce.cart.domain.model.EnrichedCartItem;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Converter for transforming use case results and the enriched cart read model into Cart DTOs.
 *
 * <p>Results deliver values ({@link Money}, {@link CartItemSummary}); this converter splits them
 * into the amount and currency-code fields of the DTOs. It derives nothing itself.
 */
@Component
public final class ShoppingCartDtoConverter {

  /** Converts the opened cart to DTO. */
  public ShoppingCartDto toDto(final CreateCartResult.Created output) {
    return new ShoppingCartDto(
        output.cartId(),
        output.customerId(),
        List.of(), // New cart has no items
        spell(output.status()),
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
        spell(cart.status()),
        total.amount(),
        total.currency().getCurrencyCode(),
        items.size());
  }

  /** Converts the cart after the item was added to DTO. */
  public ShoppingCartDto toDto(final AddItemToCartResult.Added output) {
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

  /** Converts the cart after the item was removed to DTO. */
  public ShoppingCartDto toDto(final RemoveItemFromCartResult.Removed output) {
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

  /** Converts GetAllCartsResult to list DTO. */
  public ShoppingCartListDto toListDto(final GetAllCartsResult output) {
    final List<ShoppingCartListDto.CartSummaryDto> summaries =
        output.carts().stream()
            .map(
                cart ->
                    new ShoppingCartListDto.CartSummaryDto(
                        cart.cartId(),
                        cart.customerId(),
                        spell(cart.status()),
                        cart.itemCount(),
                        cart.total().amount(),
                        cart.total().currency().getCurrencyCode(),
                        cart.items().stream().map(this::toItemDto).toList()))
            .toList();

    return new ShoppingCartListDto(summaries);
  }

  private CartItemDto toItemDto(final EnrichedCartItem item) {
    // Every answer about a cart carries today's price, from the article data
    return toItemDto(CartItemSummary.from(item));
  }

  /** A line without a unit price — its product the catalogue no longer knows — has no price. */
  private CartItemDto toItemDto(final CartItemSummary item) {
    final @Nullable Money price = item.unitPrice();
    return new CartItemDto(
        item.itemId(),
        item.productId(),
        item.quantity(),
        price == null ? null : price.amount(),
        price == null ? null : price.currency().getCurrencyCode(),
        item.available());
  }

  /**
   * Every answer spells a cart's status the same way, upper-case. Each value's spelling is written
   * out, so renaming a member never changes what a client reads.
   */
  private static String spell(final CartStatus status) {
    return switch (status) {
      case ACTIVE -> "ACTIVE";
      case COMPLETED -> "COMPLETED";
      case ABANDONED -> "ABANDONED";
    };
  }
}
