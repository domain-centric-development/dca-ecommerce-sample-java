package dev.domaincentric.sample.ecommerce.cart.application.cartrecovery.recovercart;

import dev.domaincentric.sample.ecommerce.cart.application.shared.CartItemSummary;
import dev.domaincentric.sample.ecommerce.cart.domain.model.ShoppingCart;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.Money;
import java.util.Currency;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Output model for recovering cart on login.
 *
 * @param cartId the resulting cart ID (registered user's cart)
 * @param customerId the registered user's customer ID
 * @param items the merged list of cart items
 * @param total the total cart amount after merge
 * @param itemsMerged number of items merged from anonymous cart
 * @param anonymousCartDeleted whether the anonymous cart was deleted
 */
public record RecoverCartOnLoginResult(
    @Nullable String cartId,
    String customerId,
    List<CartItemSummary> items,
    Money total,
    int itemsMerged,
    boolean anonymousCartDeleted) {

  /** Creates a response when no cart recovery was needed (no anonymous cart existed). */
  public static RecoverCartOnLoginResult noRecoveryNeeded(final String customerId) {
    return new RecoverCartOnLoginResult(
        null, customerId, List.of(), Money.zero(Currency.getInstance("EUR")), 0, false);
  }

  /** Builds the result from the registered cart as it stands after the recovery. */
  public static RecoverCartOnLoginResult recovered(final ShoppingCart cart, final int itemsMerged) {
    return new RecoverCartOnLoginResult(
        cart.id().value(),
        cart.customerId().value(),
        cart.items().stream().map(CartItemSummary::from).toList(),
        cart.calculateTotal(),
        itemsMerged,
        true);
  }
}
