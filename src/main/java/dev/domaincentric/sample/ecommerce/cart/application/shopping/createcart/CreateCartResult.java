package dev.domaincentric.sample.ecommerce.cart.application.shopping.createcart;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CartStatus;
import dev.domaincentric.sample.ecommerce.cart.domain.model.CustomerId;

/** What opening a cart came to: a new cart, or the customer already shops in another one. */
public sealed interface CreateCartResult
    permits CreateCartResult.Created, CreateCartResult.ActiveCartAlreadyExists {

  /**
   * A new cart was opened.
   *
   * @param cartId the generated cart ID
   * @param customerId the customer ID
   * @param status the cart status (always {@code ACTIVE} for new carts)
   */
  record Created(String cartId, String customerId, CartStatus status) implements CreateCartResult {}

  /**
   * The customer already has an active cart, and a customer holds one at a time. The store states
   * that rule; nothing was written.
   *
   * @param customerId the customer
   */
  record ActiveCartAlreadyExists(CustomerId customerId) implements CreateCartResult, Refusal {}
}
