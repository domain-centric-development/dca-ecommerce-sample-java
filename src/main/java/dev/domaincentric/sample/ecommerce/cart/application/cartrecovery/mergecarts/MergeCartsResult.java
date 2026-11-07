package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.mergecarts;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.List;

/**
 * What merging the carts came to: the chosen strategy was applied, or the request named a guest
 * other than the one the caller was before the login.
 */
public sealed interface MergeCartsResult
    permits MergeCartsResult.Merged, MergeCartsResult.GuestCartNotOwned {

  /**
   * The chosen strategy was applied.
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
  record Merged(
      String cartId,
      String customerId,
      List<CartItemSummary> items,
      Money total,
      CartMergeStrategy strategyApplied,
      int itemsFromAnonymous,
      int itemsFromAccount,
      boolean anonymousCartDeleted)
      implements MergeCartsResult {

    /** Builds the case from the account cart as it stands after the merge. */
    public static Merged from(
        final ShoppingCart cart,
        final CartMergeStrategy strategy,
        final int itemsFromAnonymous,
        final int itemsFromAccount,
        final boolean anonymousCartDeleted) {
      return new Merged(
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

  /**
   * The request named a guest other than the one the visitor was before their login: only that
   * guest's cart may follow into the account (ADR-036). Nothing was read or changed.
   *
   * @param guest the guest the request named
   */
  record GuestCartNotOwned(CustomerId guest) implements MergeCartsResult, Refusal {}
}
