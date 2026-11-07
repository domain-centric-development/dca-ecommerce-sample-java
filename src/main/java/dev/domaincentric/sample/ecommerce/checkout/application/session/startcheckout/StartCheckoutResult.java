package dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout;

import dev.domaincentric.dca.buildingblocks.application.Refusal;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CartId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSession;
import dev.domaincentric.sample.ecommerce.sharedkernel.domain.model.ProductId;
import java.util.List;

/**
 * What starting a checkout came to: the session started, or the refusal — a case of the result, not
 * an exception. Every refusing case is decided before a session exists.
 */
public sealed interface StartCheckoutResult
    permits StartCheckoutResult.Started,
        StartCheckoutResult.CartNotAvailable,
        StartCheckoutResult.CartNotActive,
        StartCheckoutResult.EmptyCart,
        StartCheckoutResult.ArticleNotAvailable,
        StartCheckoutResult.CheckoutItemsUnavailable {

  /**
   * The checkout session was started.
   *
   * <p>A command's result is small: the session, where the checkout stands now and what state it is
   * in. The page that follows asks the session query for everything it displays.
   *
   * @param sessionId the checkout session ID
   * @param currentStep the current step after the command
   * @param status the session status
   */
  record Started(String sessionId, String currentStep, String status)
      implements StartCheckoutResult {

    /**
     * Builds the case from the session as it stands after the command.
     *
     * @param session the started checkout session
     * @return the case
     */
    public static Started from(final CheckoutSession session) {
      return new Started(
          session.id().value(), session.currentStep().name(), session.status().name());
    }
  }

  /**
   * The cart is not available to the asking customer — it does not exist or is somebody else's;
   * both are reported alike (ADR-036).
   *
   * @param cartId the cart the caller named
   */
  record CartNotAvailable(CartId cartId) implements StartCheckoutResult, Refusal {}

  /**
   * The cart is no longer active: a completed or abandoned cart has had its decision.
   *
   * @param cartId the cart the caller named
   */
  record CartNotActive(CartId cartId) implements StartCheckoutResult, Refusal {}

  /**
   * The cart holds nothing to check out — read before the session exists, and the session's own
   * rule as well.
   *
   * @param cartId the cart the caller named
   */
  record EmptyCart(CartId cartId) implements StartCheckoutResult, Refusal {}

  /**
   * The article facts a line item needs are not available for a product of the cart.
   *
   * @param productId the product the answer does not carry
   */
  record ArticleNotAvailable(ProductId productId) implements StartCheckoutResult, Refusal {}

  /**
   * Some of the cart's positions cannot be bought — out of stock, withdrawn, priced differently.
   *
   * @param productIds the positions that refused, in cart order
   */
  record CheckoutItemsUnavailable(List<ProductId> productIds)
      implements StartCheckoutResult, Refusal {

    public CheckoutItemsUnavailable {
      productIds = List.copyOf(productIds);
    }
  }
}
