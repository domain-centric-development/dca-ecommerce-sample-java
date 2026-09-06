package dev.domaincentric.sample.ecommerce.cart.application.completecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for completing a shopping cart after checkout confirmation.
 *
 * <p>This use case is triggered when a checkout is confirmed (CheckoutConfirmed event), marking the
 * cart as completed to indicate the order has been placed.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CompleteCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class CompleteCartUseCase implements CompleteCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public CompleteCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public CompleteCartResult execute(final CompleteCartCommand input) {
    final CartId cartId = CartId.of(input.cartId());

    // Retrieve cart
    final ShoppingCart cart =
        shoppingCartRepository
            .findById(cartId)
            .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + input.cartId()));

    // Complete cart (business logic validates status)
    cart.complete();

    // Persist
    shoppingCartRepository.save(cart);

    eventPublisher.publishAndClearEvents(cart);

    return new CompleteCartResult(cart.id().value().toString(), cart.status().name());
  }
}
