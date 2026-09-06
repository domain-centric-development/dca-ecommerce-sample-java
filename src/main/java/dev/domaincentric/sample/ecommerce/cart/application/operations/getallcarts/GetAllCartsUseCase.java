package dev.domaincentric.sample.ecommerce.cart.application.getallcarts;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving all shopping carts.
 *
 * <p>This is a query use case that retrieves all carts without modifying state.
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link GetAllCartsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional(readOnly = true)
public class GetAllCartsUseCase implements GetAllCartsInputPort {

  private final ShoppingCartRepository shoppingCartRepository;

  public GetAllCartsUseCase(final ShoppingCartRepository shoppingCartRepository) {
    this.shoppingCartRepository = shoppingCartRepository;
  }

  @Override
  public GetAllCartsResult execute(final GetAllCartsQuery input) {
    final List<ShoppingCart> carts = shoppingCartRepository.findAll();

    final List<GetAllCartsResult.CartSummary> cartSummaries =
        carts.stream()
            .map(
                cart -> {
                  final Money total = cart.calculateTotal();
                  return new GetAllCartsResult.CartSummary(
                      cart.id().value(),
                      cart.customerId().value(),
                      cart.status().name(),
                      cart.items().size(),
                      total.amount(),
                      total.currency().getCurrencyCode());
                })
            .toList();

    return new GetAllCartsResult(cartSummaries);
  }
}
