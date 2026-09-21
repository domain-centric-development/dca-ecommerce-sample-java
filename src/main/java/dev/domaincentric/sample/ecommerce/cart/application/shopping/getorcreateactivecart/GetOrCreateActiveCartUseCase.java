package dev.domaincentric.sample.ecommerce.cart.application.shopping.getorcreateactivecart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.Optional;
import org.springframework.stereotype.Service;

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
public class GetOrCreateActiveCartUseCase implements GetOrCreateActiveCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public GetOrCreateActiveCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
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

    // Create new cart. The store refuses a second active cart for the same customer, so a request
    // that lost the race takes the cart that won rather than adding one of its own.
    //
    // The claim is attempted in a transaction of its own, and the recovery happens outside it. A
    // store that refuses a write mid-transaction leaves that transaction unusable - a relational
    // one has to roll it back before anything else can be read - so a catch that carried on inside
    // it would read fine and then fail at commit. The boundary is drawn here for that reason, which
    // is why this use case carries no transactional annotation.
    final CartId newCartId = CartId.generate();
    try {
      return transactionBoundary.inTransaction(
          () -> {
            final ShoppingCart newCart = new ShoppingCart(newCartId, customerId);
            shoppingCartRepository.save(newCart);
            eventPublisher.publishAndClearEvents(newCart);
            return new GetOrCreateActiveCartResult(newCartId.value(), customerId.value(), true);
          });
    } catch (final ActiveCartAlreadyExistsException alreadyActive) {
      final ShoppingCart winner =
          shoppingCartRepository
              .findActiveCartByCustomerId(customerId)
              .orElseThrow(() -> alreadyActive);
      return new GetOrCreateActiveCartResult(winner.id().value(), customerId.value(), false);
    }
  }
}
