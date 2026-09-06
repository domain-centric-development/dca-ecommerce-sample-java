package dev.domaincentric.sample.ecommerce.cart.application.mergecarts;

import dev.domaincentric.dca.buildingblocks.hexagonal.port.out.DomainEventPublisher;
import dev.domaincentric.sample.ecommerce.cart.application.shared.ShoppingCartRepository;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartItem;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for merging carts based on user's chosen strategy.
 *
 * <p>When a user logs in and both their anonymous cart and account cart have items, this use case
 * executes the user's chosen strategy:
 *
 * <ul>
 *   <li>MERGE_BOTH: Combine items from both carts
 *   <li>USE_ACCOUNT_CART: Keep account cart, discard anonymous cart
 *   <li>USE_ANONYMOUS_CART: Replace account cart with anonymous cart items
 * </ul>
 *
 * <p><b>Hexagonal Architecture:</b> This class implements the {@link MergeCartsInputPort}
 * interface, which is a primary/driving port in the application layer.
 */
@Service
@Transactional
public class MergeCartsUseCase implements MergeCartsInputPort {

  private final ShoppingCartRepository shoppingCartRepository;
  private final DomainEventPublisher eventPublisher;

  public MergeCartsUseCase(
      final ShoppingCartRepository shoppingCartRepository,
      final DomainEventPublisher eventPublisher) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public MergeCartsResult execute(final MergeCartsCommand input) {
    final CustomerId anonymousCustomerId = CustomerId.of(input.anonymousUserId());
    final CustomerId registeredCustomerId = CustomerId.of(input.registeredUserId());

    // Find both carts
    final Optional<ShoppingCart> anonymousCartOpt =
        shoppingCartRepository.findActiveCartByCustomerId(anonymousCustomerId);
    final Optional<ShoppingCart> accountCartOpt =
        shoppingCartRepository.findActiveCartByCustomerId(registeredCustomerId);

    // Get or create account cart
    final ShoppingCart accountCart =
        accountCartOpt.orElseGet(
            () -> {
              final ShoppingCart newCart =
                  new ShoppingCart(CartId.generate(), registeredCustomerId);
              return shoppingCartRepository.save(newCart);
            });

    final int originalAccountItems = accountCart.itemCount();

    return switch (input.strategy()) {
      case MERGE_BOTH -> mergeBothCarts(anonymousCartOpt, accountCart, input.strategy());
      case USE_ACCOUNT_CART ->
          useAccountCartOnly(anonymousCartOpt, accountCart, originalAccountItems, input.strategy());
      case USE_ANONYMOUS_CART ->
          useAnonymousCartOnly(anonymousCartOpt, accountCart, input.strategy());
    };
  }

  private MergeCartsResult mergeBothCarts(
      final Optional<ShoppingCart> anonymousCartOpt,
      final ShoppingCart accountCart,
      final CartMergeStrategy strategy) {

    int itemsFromAnonymous = 0;
    final int itemsFromAccount = accountCart.itemCount();

    if (anonymousCartOpt.isPresent()) {
      final ShoppingCart anonymousCart = anonymousCartOpt.get();

      // Add all items from anonymous cart to account cart
      for (final CartItem item : anonymousCart.items()) {
        accountCart.addItem(item.productId(), item.quantity(), item.priceAtAddition());
        itemsFromAnonymous++;
      }

      // Save merged cart
      shoppingCartRepository.save(accountCart);

      // Publish domain events
      eventPublisher.publishAndClearEvents(accountCart);

      // Delete anonymous cart
      shoppingCartRepository.deleteById(anonymousCart.id());
    }

    return buildResponse(accountCart, strategy, itemsFromAnonymous, itemsFromAccount, true);
  }

  private MergeCartsResult useAccountCartOnly(
      final Optional<ShoppingCart> anonymousCartOpt,
      final ShoppingCart accountCart,
      final int originalAccountItems,
      final CartMergeStrategy strategy) {

    // Just delete the anonymous cart if it exists
    if (anonymousCartOpt.isPresent()) {
      shoppingCartRepository.deleteById(anonymousCartOpt.get().id());
    }

    return buildResponse(accountCart, strategy, 0, originalAccountItems, true);
  }

  private MergeCartsResult useAnonymousCartOnly(
      final Optional<ShoppingCart> anonymousCartOpt,
      final ShoppingCart accountCart,
      final CartMergeStrategy strategy) {

    int itemsFromAnonymous = 0;

    if (anonymousCartOpt.isPresent()) {
      final ShoppingCart anonymousCart = anonymousCartOpt.get();
      itemsFromAnonymous = anonymousCart.itemCount();

      // Clear account cart
      if (!accountCart.isEmpty()) {
        accountCart.clear();
      }

      // Copy items from anonymous cart to account cart
      for (final CartItem item : anonymousCart.items()) {
        accountCart.addItem(item.productId(), item.quantity(), item.priceAtAddition());
      }

      // Save account cart
      shoppingCartRepository.save(accountCart);

      // Publish domain events
      eventPublisher.publishAndClearEvents(accountCart);

      // Delete anonymous cart
      shoppingCartRepository.deleteById(anonymousCart.id());
    }

    return buildResponse(accountCart, strategy, itemsFromAnonymous, 0, true);
  }

  private MergeCartsResult buildResponse(
      final ShoppingCart cart,
      final CartMergeStrategy strategy,
      final int itemsFromAnonymous,
      final int itemsFromAccount,
      final boolean anonymousCartDeleted) {

    final List<MergeCartsResult.CartItemSummary> items =
        cart.items().stream()
            .map(
                item ->
                    new MergeCartsResult.CartItemSummary(
                        item.id().value(),
                        item.productId().value(),
                        item.quantity().value(),
                        item.priceAtAddition().value().amount(),
                        item.priceAtAddition().value().currency().getCurrencyCode()))
            .toList();

    final Money total = cart.calculateTotal();

    return new MergeCartsResult(
        cart.id().value(),
        cart.customerId().value(),
        items,
        total.amount(),
        total.currency().getCurrencyCode(),
        strategy,
        itemsFromAnonymous,
        itemsFromAccount,
        anonymousCartDeleted);
  }
}
