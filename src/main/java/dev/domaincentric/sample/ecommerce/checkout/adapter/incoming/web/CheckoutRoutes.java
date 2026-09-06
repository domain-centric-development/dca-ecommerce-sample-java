package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;

/**
 * The routes of the checkout pages, and how a {@link StepAccess} decision maps onto them.
 *
 * <p>The domain answers in steps; this is where a step becomes a URL. {@code BUYER_INFO} is served
 * at {@code /checkout/buyer}.
 */
public final class CheckoutRoutes {

  public static final String CART = "/cart";
  private static final String CHECKOUT = "/checkout";

  private CheckoutRoutes() {}

  /**
   * The Spring MVC redirect for a denied step access.
   *
   * @param access a decision that is not granted
   * @return {@code redirect:} followed by the cart or the page of the step to show instead
   */
  public static String redirectFor(final StepAccess access) {
    if (access.granted()) {
      throw new IllegalArgumentException("Access is granted, nothing to redirect to");
    }
    return "redirect:" + (access.isBackToCart() ? CART : pathOf(access.redirectStep()));
  }

  /**
   * The page of a checkout step.
   *
   * @param step the step
   * @return its path below {@code /checkout}
   */
  public static String pathOf(final CheckoutStep step) {
    final String page =
        switch (step) {
          case BUYER_INFO -> "buyer";
          case DELIVERY -> "delivery";
          case PAYMENT -> "payment";
          case REVIEW -> "review";
          case CONFIRMATION -> "confirmation";
        };
    return CHECKOUT + "/" + page;
  }
}
