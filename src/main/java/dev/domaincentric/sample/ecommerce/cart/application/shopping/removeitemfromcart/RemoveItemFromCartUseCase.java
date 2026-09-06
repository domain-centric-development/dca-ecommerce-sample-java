package dev.domaincentric.sample.ecommerce.cart.application.removeitemfromcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for removing an item from a shopping cart.
 *
 * <p>This use case orchestrates removing an item from the cart by:
 *
 * <ol>
 *   <li>Retrieving the cart
 *   <li>Removing the item (business logic in aggregate)
 *   <li>Persisting the updated cart
 *   <li>Publishing domain events
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RemoveItemFromCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class RemoveItemFromCartUseCase implements RemoveItemFromCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RemoveItemFromCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public RemoveItemFromCartResult execute(final RemoveItemFromCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());
    final ProductId productId = ProductId.of(input.productId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findByIdForCustomer(cartId, CustomerId.of(input.customerId()))
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Remove item from cart (business logic in aggregate)
    cart.removeItemByProductId(productId);

    // Persist
    shoppingCartRepository.save(cart);

    // Publish domain events
    eventPublisher.publishAndClearEvents(cart);

    // Map to output
    final List<RemoveItemFromCartResult.CartItemSummary> items =
        cart.items().stream()
            .map(
                item ->
                    new RemoveItemFromCartResult.CartItemSummary(
                        item.id().value().toString(),
                        item.productId().value().toString(),
                        item.quantity().value(),
                        item.priceAtAddition().value().amount(),
                        item.priceAtAddition().value().currency().getCurrencyCode()))
            .toList();

    final Money total = cart.calculateTotal();

    return new RemoveItemFromCartResult(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode());
  }
}
