package dev.domaincentric.sample.ecommerce.cart.application.getorcreateactivecart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for getting or creating an active cart for a customer.
 *
 * <p>This use case either retrieves an existing active cart for the customer or creates a new one
 * if no active cart exists.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link
 * GetOrCreateActiveCartInputPort} interface, which is a primary/driving port in the application
 * layer.
 */
@Service
@Transactional
public class GetOrCreateActiveCartUseCase implements GetOrCreateActiveCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public GetOrCreateActiveCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public GetOrCreateActiveCartResult execute(final GetOrCreateActiveCartCommand input) {
    final CustomerId customerId = CustomerId.of(input.customerId());

    // Try to find existing active cart
    final Optional<ShoppingCart> existingCart =
        shoppingCartRepository.findActiveCartByCustomerId(customerId);

    if (existingCart.isPresent()) {
      // Return existing cart
      return new GetOrCreateActiveCartResult(
          existingCart.get().id().value(), customerId.value(), false);
    }

    // Create new cart
    final CartId newCartId = CartId.generate();
    final ShoppingCart newCart = new ShoppingCart(newCartId, customerId);
    shoppingCartRepository.save(newCart);

    eventPublisher.publishAndClearEvents(newCart);

    return new GetOrCreateActiveCartResult(newCartId.value(), customerId.value(), true);
  }
}
