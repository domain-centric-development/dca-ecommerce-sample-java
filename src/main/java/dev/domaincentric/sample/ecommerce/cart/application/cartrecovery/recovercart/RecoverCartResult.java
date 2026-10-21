package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.Currency;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Output model for recovering a guest cart.
 *
 * @param cartId the resulting cart ID (the registered user's cart, or the guest cart they took
 *     over)
 * @param customerId the registered user's customer ID
 * @param items the cart items after the recovery
 * @param total the total cart amount after the recovery
 * @param itemsMerged number of items the registered user's cart gained from the anonymous cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record RecoverCartResult(
    @Nullable String cartId,
    String customerId,
    List<CartItemSummary> items,
    Money total,
    int itemsMerged,
    boolean anonymousCartDeleted) {

  /** Creates a response when no cart recovery was needed (no anonymous cart existed). */
  public static RecoverCartResult noRecoveryNeeded(final String customerId) {
    return new RecoverCartResult(
        null, customerId, List.of(), Money.zero(Currency.getInstance("EUR")), 0, false);
  }

  /** Builds the result from the guest cart the account took over, which was not deleted. */
  public static RecoverCartResult takenOver(final ShoppingCart cart) {
    return of(cart, cart.itemCount(), false);
  }

  /** Builds the result from the registered cart as it stands after the recovery. */
  public static RecoverCartResult recovered(final ShoppingCart cart, final int itemsMerged) {
    return of(cart, itemsMerged, true);
  }

  private static RecoverCartResult of(
      final ShoppingCart cart, final int itemsMerged, final boolean anonymousCartDeleted) {
    return new RecoverCartResult(
        cart.id().value(),
        cart.customerId().value(),
        cart.items().stream().map(CartItemSummary::from).toList(),
        cart.calculateTotal(),
        itemsMerged,
        anonymousCartDeleted);
  }
}
