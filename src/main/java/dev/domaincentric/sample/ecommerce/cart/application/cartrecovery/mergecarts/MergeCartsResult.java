package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * Response from the cart merge operation.
 *
 * @param cartId the resulting cart ID (account cart)
 * @param customerId the registered user's customer ID
 * @param items the final list of cart items
 * @param total the total cart amount
 * @param strategyApplied the strategy that was applied
 * @param itemsFromAnonymous number of items taken from anonymous cart
 * @param itemsFromAccount number of items kept from account cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record MergeCartsResult(
    String cartId,
    String customerId,
    List<CartItemSummary> items,
    Money total,
    CartMergeStrategy strategyApplied,
    int itemsFromAnonymous,
    int itemsFromAccount,
    boolean anonymousCartDeleted) {

  /** Builds the result from the account cart as it stands after the merge. */
  public static MergeCartsResult from(
      final ShoppingCart cart,
      final CartMergeStrategy strategy,
      final int itemsFromAnonymous,
      final int itemsFromAccount,
      final boolean anonymousCartDeleted) {
    return new MergeCartsResult(
        cart.id().value(),
        cart.customerId().value(),
        cart.items().stream().map(CartItemSummary::from).toList(),
        cart.calculateTotal(),
        strategy,
        itemsFromAnonymous,
        itemsFromAccount,
        anonymousCartDeleted);
  }
}
