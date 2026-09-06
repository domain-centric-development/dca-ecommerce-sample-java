package dev.domaincentric.sample.ecommerce.checkout.application.startcheckout;

/**
 * Input model for starting a checkout session.
 *
 * <p>The cart ID arrives from the browser, so it names <i>which</i> cart — never <i>whose</i>.
 * Without the customer the command would let anyone start a checkout on a cart whose ID they
 * happened to learn.
 *
 * @param cartId the ID of the cart to checkout
 * @param customerId the customer the caller is acting as
 */
public record StartCheckoutCommand(String cartId, String customerId) {

  /** Compact constructor with validation. */
  public StartCheckoutCommand {
    if (cartId == null || cartId.isBlank()) {
      throw new IllegalArgumentException("Cart ID cannot be null or blank");
    }
    if (customerId == null || customerId.isBlank()) {
      throw new IllegalArgumentException("Customer ID cannot be null or blank");
    }
  }
}
