package dev.domaincentric.sample.ecommerce.cart.application.cartcheckout.completecart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;

/** What completing a cart after checkout confirmation came to. */
public sealed interface CompleteCartResult
    permits CompleteCartResult.Completed, CompleteCartResult.CartNotFound {

  /**
   * The cart is reconciled with the confirmed checkout.
   *
   * @param cartId the cart ID
   * @param status the cart status after the completion
   * @param alreadyCompleted whether the event had completed the cart before, so nothing changed
   */
  record Completed(String cartId, CartStatus status, boolean alreadyCompleted)
      implements CompleteCartResult {}

  /**
   * There is no cart under that identity.
   *
   * @param cartId the cart the caller named
   */
  record CartNotFound(CartId cartId) implements CompleteCartResult, Refusal {}
}
