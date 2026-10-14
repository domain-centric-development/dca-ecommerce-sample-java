package dev.domaincentric.sample.ecommerce.cart.application.shopping.getactivecart;

import java.util.Optional;

/**
 * The customer's active cart, or none.
 *
 * @param cartId the active cart's ID, empty when the customer has no active cart
 */
public record GetActiveCartResult(Optional<String> cartId) {

  public static GetActiveCartResult of(final String cartId) {
    return new GetActiveCartResult(Optional.of(cartId));
  }

  public static GetActiveCartResult none() {
    return new GetActiveCartResult(Optional.empty());
  }
}
