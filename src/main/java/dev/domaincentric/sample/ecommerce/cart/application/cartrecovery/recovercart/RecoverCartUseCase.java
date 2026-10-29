package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.GuestCartNotOwnedException;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for recovering a guest cart into the account's cart.
 *
 * <p>When a registered user logs in from a new device/browser:
 *
 * <ol>
 *   <li>Find cart for anonymous user's UserId (if different)
 *   <li>Find cart for registered user's UserId
 *   <li>If the registered user's cart is absent or holds nothing, the anonymous cart itself becomes
 *       theirs (an empty registered cart is deleted)
 *   <li>Otherwise, merge items from anonymous cart into registered cart and delete the anonymous
 *       cart after merge
 * </ol>
 *
 * <p>Only the cart of the guest the caller was before the login is taken over: a command that names
 * another guest is refused with {@link GuestCartNotOwnedException} before any cart is read (ADR-036
 * — ownership is the use case's).
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link RecoverCartInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class RecoverCartUseCase implements RecoverCartInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public RecoverCartUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public RecoverCartResult execute(final RecoverCartCommand input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    if (!input.anonymousUserId().equals(input.guestBeforeLogin())) {
      throw new GuestCartNotOwnedException(anonymousCustomerId);
    }

    // If same user ID, no merge needed
    if (anonymousCustomerId.equals(registeredCustomerId)) {
      return RecoverCartResult.noRecoveryNeeded(registeredCustomerId.value());
    }

    // Find anonymous user's active cart
    final Optional<ShoppingCart> anonymousCart =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);

    // If no anonymous cart exists, no recovery needed
    if (anonymousCart.isEmpty() || anonymousCart.get().isEmpty()) {
      return RecoverCartResult.noRecoveryNeeded(registeredCustomerId.value());
    }

    final ShoppingCart anonCart = anonymousCart.get();
    final Optional<ShoppingCart> accountCart =
        shoppingCartRepository.findActiveCartByCustomerId(registeredCustomerId);

    // An account whose cart holds nothing takes the guest cart itself over: same id, same lines
    if (accountCart.isEmpty() || accountCart.get().isEmpty()) {
      accountCart.ifPresent(emptyCart -> shoppingCartRepository.deleteById(emptyCart.id()));
      anonCart.takeOverBy(registeredCustomerId);
      shoppingCartRepository.save(anonCart);
      return RecoverCartResult.takenOver(anonCart);
    }

    // Merge items from anonymous cart into the account's cart
    final ShoppingCart registeredCart = accountCart.get();
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

    return RecoverCartResult.recovered(registeredCart, itemsMerged);
  }
}
