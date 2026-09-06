package dev.domaincentric.sample.ecommerce.cart.application.createcart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new shopping cart.
 *
 * <p>This use case creates a new empty shopping cart for a customer.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CreateCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class CreateCartUseCase implements CreateCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public CreateCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public CreateCartResult execute(final CreateCartCommand input) {
    final CustomerId customerId = new CustomerId(input.customerId());
    final CartId cartId = CartId.generate();

    // Create cart aggregate
    final ShoppingCart cart = new ShoppingCart(cartId, customerId);

    // Persist
    shoppingCartRepository.save(cart);

    eventPublisher.publishAndClearEvents(cart);

    // Map to output
    return new CreateCartResult(
        cart.id().value().toString(), cart.customerId().value(), cart.status().name());
  }
}
