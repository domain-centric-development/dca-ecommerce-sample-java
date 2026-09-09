package dev.domaincentric.sample.ecommerce.checkout.adapter.outgoing.cart;

import dev.domaincentric.sample.ecommerce.cart.api.CartService;
import dev.domaincentric.sample.ecommerce.cart.api.CartService.CartSnapshot;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartData;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartDataPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements the CartDataPort by accessing the Cart bounded context's API.
 *
 * <p>This is the Anti-Corruption Layer (ACL) that translates between the Cart context's published
 * API and the Checkout context's data structures. All coupling to the Cart context is isolated here
 * and goes through the Cart module's published {@link CartService} API.
 */
@Component
public class CartDataAdapter implements CartDataPort {

  private final CartService cartService;

  public CartDataAdapter(final CartService cartService) {
    this.cartService = cartService;
  }

  @Override
  public Optional<CartData> findById(final CartId cartId, final CustomerId customerId) {
    return cartService
        .findCartById(UUID.fromString(cartId.value()), customerId.value())
        .map(this::toCartData);
  }

  private CartData toCartData(final CartSnapshot snapshot) {
    final java.util.List<CartData.CartItemData> items =
        snapshot.items().stream()
            .map(
                item ->
                    new CartData.CartItemData(
                        item.productId(),
                        item.priceAtAddition(),
                        item.quantity(),
                        item.positionSnapshot()))
            .toList();

    return new CartData(
        CartId.of(snapshot.cartId().toString()),
        CustomerId.of(snapshot.customerId()),
        items,
        snapshot.active());
  }
}
