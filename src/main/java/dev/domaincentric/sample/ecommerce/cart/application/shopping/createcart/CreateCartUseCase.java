package dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart;

import dev.domaincentric.dca.buildingblocks.application.TransactionBoundary;
import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ActiveCartAlreadyExistsException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult.ActiveCartAlreadyExists;
import dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart.CreateCartResult.Created;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import org.springframework.stereotype.Service;

/**
 * Use case for creating a new shopping cart.
 *
 * <p>This use case creates a new empty shopping cart for a customer. A customer holds one active
 * cart at a time; the store states that rule and refuses the second one, which this use case
 * returns as {@link ActiveCartAlreadyExists}.
 *
 * <p>The store refuses the row mid-transaction, which leaves that transaction unusable. The use
 * case catches the failure the port declares where it arises and returns the case from inside the
 * {@link TransactionBoundary}: the case is a {@link
 * dev.domaincentric.dca.buildingblocks.application.Refusal}, so the boundary rolls the transaction
 * back as the thrown failure would have.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link CreateCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
public class CreateCartUseCase implements CreateCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;
  private final TransactionBoundary transactionBoundary;

  public CreateCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher,
      final TransactionBoundary transactionBoundary) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
    this.transactionBoundary = transactionBoundary;
  }

  @Override
  public CreateCartResult execute(final CreateCartCommand input) {
    final CustomerId customerId = new CustomerId(input.customerId());
    final CartId cartId = CartId.generate();

    return transactionBoundary.inTransaction(
        () -> {
          // Create cart aggregate
          final ShoppingCart cart = ShoppingCart.create(cartId, customerId);

          // Persist - the store refuses a second active cart of the customer
          try {
            shoppingCartRepository.save(cart);
          } catch (final ActiveCartAlreadyExistsException alreadyActive) {
            return new ActiveCartAlreadyExists(alreadyActive.customerId());
          }

          eventPublisher.publishAndClearEvents(cart);

          // Map to output
          return new Created(
              cart.id().value().toString(), cart.customerId().value(), cart.status());
        });
  }
}
