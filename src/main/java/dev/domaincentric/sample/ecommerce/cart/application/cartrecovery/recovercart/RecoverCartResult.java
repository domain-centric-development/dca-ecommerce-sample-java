package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.Currency;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * What recovering a guest cart came to: the recovery ran — whether or not there was anything to
 * bring — or the request named a guest other than the one the caller was before the login.
 */
public sealed interface RecoverCartResult
    permits RecoverCartResult.Recovered, RecoverCartResult.GuestCartNotOwned {

  /**
   * The recovery ran.
   *
   * @param cartId the resulting cart ID (the registered user's cart, or the guest cart they took
   *     over); {@code null} when there was nothing to recover
   * @param customerId the registered user's customer ID
   * @param items the cart items after the recovery
   * @param total the total cart amount after the recovery
   * @param itemsMerged number of items the registered user's cart gained from the anonymous cart
   * @param anonymousCartDeleted whether the anonymous cart was deleted
   */
  record Recovered(
      @Nullable String cartId,
      String customerId,
      List<CartItemSummary> items,
      Money total,
      int itemsMerged,
      boolean anonymousCartDeleted)
      implements RecoverCartResult {

    /** No cart recovery was needed (no anonymous cart with items existed). */
    public static Recovered noRecoveryNeeded(final String customerId) {
      return new Recovered(
          null, customerId, List.of(), Money.zero(Currency.getInstance("EUR")), 0, false);
    }

    /** Builds the case from the guest cart the account took over, which was not deleted. */
    public static Recovered takenOver(final ShoppingCart cart) {
      return of(cart, cart.itemCount(), false);
    }

    /** Builds the case from the registered cart as it stands after the recovery. */
    public static Recovered recovered(final ShoppingCart cart, final int itemsMerged) {
      return of(cart, itemsMerged, true);
    }

    private static Recovered of(
        final ShoppingCart cart, final int itemsMerged, final boolean anonymousCartDeleted) {
      return new Recovered(
          cart.id().value(),
          cart.customerId().value(),
          cart.items().stream().map(CartItemSummary::from).toList(),
          cart.calculateTotal(),
          itemsMerged,
          anonymousCartDeleted);
    }
  }

  /**
   * The request named a guest other than the one the visitor was before their login: only that
   * guest's cart may follow into the account (ADR-036). Nothing was read or changed.
   *
   * @param guest the guest the request named
   */
  record GuestCartNotOwned(CustomerId guest) implements RecoverCartResult, Refusal {}
}
