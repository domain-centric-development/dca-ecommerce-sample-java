package dev.domaincentric.sample.ecommerce.cart.application.shopping.removecartitem;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartNotFoundException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItemId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for taking one line out of a shopping cart, named by the line's own id.
 *
 * <p>Loads the caller's cart — another customer's cart is not found — and lets the aggregate remove
 * the line, which refuses a line the cart does not hold; then persists the cart and publishes its
 * domain events.
 */
@Service
@Transactional
public class RemoveCartItemUseCase implements RemoveCartItemInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RemoveCartItemUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public RemoveCartItemResult execute(final RemoveCartItemCommand input) {
    final CartId cartId = CartId.of(input.cartId());
    final CartItemId cartItemId = CartItemId.of(input.cartItemId());

    final ShoppingCart cart =
        shoppingCartRepository
            .findByIdForCustomer(cartId, CustomerId.of(input.customerId()))
            .orElseThrow(() -> new CartNotFoundException(cartId));

    cart.removeItem(cartItemId);

    shoppingCartRepository.save(cart);
    eventPublisher.publishAndClearEvents(cart);

    return RemoveCartItemResult.from(cart);
  }
}
