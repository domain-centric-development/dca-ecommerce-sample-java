package dev.domaincentric.sample.ecommerce.cart.application.operations.getallcarts;

import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
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

    return GetAllCartsResult.from(carts);
  }
}
