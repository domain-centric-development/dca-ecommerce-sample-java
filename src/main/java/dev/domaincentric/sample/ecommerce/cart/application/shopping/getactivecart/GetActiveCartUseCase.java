package dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import org.springframework.stereotype.Service;

/**
 * Use case for reading the customer's active cart. It has no side effect: a customer without an
 * active cart gets none, and no cart is opened for them.
 */
@Service
public class GetActiveCartUseCase implements GetActiveCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;

  public GetActiveCartUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public GetActiveCartResult execute(final GetActiveCartQuery input) {
    return shoppingCartRepository
        .findActiveCartByCustomerId(CustomerId.of(input.customerId()))
        .map(cart -> GetActiveCartResult.of(cart.id().value()))
        .orElseGet(GetActiveCartResult::none);
  }
}
