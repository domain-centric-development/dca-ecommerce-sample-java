package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for recovering cart on login.
 *
 * <p>When a registered user logs in from a new device/browser:
 *
 * <ol>
 *   <li>Find cart for registered user's UserId
 *   <li>Find cart for anonymous user's UserId (if different)
 *   <li>If both exist, merge items from anonymous cart into registered cart
 *   <li>Delete the anonymous cart after merge
 * </ol>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RecoverCartOnLoginInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class RecoverCartOnLoginUseCase implements RecoverCartOnLoginInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RecoverCartOnLoginUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public RecoverCartOnLoginResult execute(final RecoverCartOnLoginCommand input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    // If same user ID, no merge needed
    if (anonymousCustomerId.equals(registeredCustomerId)) {
      return RecoverCartOnLoginResult.noRecoveryNeeded(registeredCustomerId.value());
    }

    // Find anonymous user's active cart
    final Optional<ShoppingCart> anonymousCart =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);

    // If no anonymous cart exists, no recovery needed
    if (anonymousCart.isEmpty() || anonymousCart.get().isEmpty()) {
      return RecoverCartOnLoginResult.noRecoveryNeeded(registeredCustomerId.value());
    }

    // Find or create registered user's active cart
    final ShoppingCart registeredCart =
        shoppingCartRepository
            .findActiveCartByCustomerId(registeredCustomerId)
            .orElseGet(
                () -> {
                  final ShoppingCart newCart =
                      new ShoppingCart(CartId.generate(), registeredCustomerId);
                  return shoppingCartRepository.save(newCart);
                });

    // Merge items from anonymous cart into registered cart
    final ShoppingCart anonCart = anonymousCart.get();
    int itemsMerged = 0;

    for (final CartItem item : anonCart.items()) {
      registeredCart.addItem(item.productId(), item.quantity(), item.priceAtAddition());
      itemsMerged++;
    }

    // Save the merged cart
    shoppingCartRepository.save(registeredCart);

    // Publish domain events from the registered cart
    eventPublisher.publishAndClearEvents(registeredCart);

    // Delete the anonymous cart
    shoppingCartRepository.deleteById(anonCart.id());

    return RecoverCartOnLoginResult.recovered(registeredCart, itemsMerged);
  }
}
