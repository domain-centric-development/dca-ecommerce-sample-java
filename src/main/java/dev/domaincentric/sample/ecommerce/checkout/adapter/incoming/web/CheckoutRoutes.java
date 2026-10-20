package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The routes of the checkout pages, and how a {@link StepAccess} decision maps onto them.
 *
 * <p>The domain answers in steps; this is where a step becomes a URL. {@code BUYER_INFO} is served
 * at {@code /checkout/buyer}. A notice for the page a redirect leads to goes into the redirect's
 * flash attributes, never into the address.
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
   * The Spring MVC redirect for a denied step access, handing a notice to the step shown instead.
   * The cart is not a checkout page: a redirect there hands it none.
   *
   * @param access a decision that is not granted
   * @param notice the notice the step shown instead says
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the cart or the page of the step
   */
  public static String redirectFor(
      final StepAccess access,
      final CheckoutStepNotice notice,
      final RedirectAttributes attributes) {
    if (access.granted() || access.isBackToCart()) {
      return redirectFor(access);
    }
    return redirectTo(access.redirectStep(), notice, attributes);
  }

  /**
   * The Spring MVC redirect to a step's page, handing it a notice.
   *
   * @param step the step
   * @param notice the notice the page says
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the step's page
   */
  public static String redirectTo(
      final CheckoutStep step,
      final CheckoutStepNotice notice,
      final RedirectAttributes attributes) {
    notice.addTo(attributes);
    return "redirect:" + pathOf(step);
  }

  /**
   * The Spring MVC redirect to a step's page, handing it a notice and the number it names.
   *
   * @param step the step
   * @param notice the notice the page says
   * @param count the number the notice names
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the step's page
   */
  public static String redirectTo(
      final CheckoutStep step,
      final CheckoutStepNotice notice,
      final int count,
      final RedirectAttributes attributes) {
    notice.addTo(attributes, count);
    return "redirect:" + pathOf(step);
  }

  /**
   * The Spring MVC redirect to a step's page, handing it a notice and the product name it names.
   *
   * @param step the step
   * @param notice the notice the page says
   * @param name the product name the notice names
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the step's page
   */
  public static String redirectTo(
      final CheckoutStep step,
      final CheckoutStepNotice notice,
      final String name,
      final RedirectAttributes attributes) {
    notice.addTo(attributes, name);
    return "redirect:" + pathOf(step);
  }

  /**
   * The Spring MVC redirect to the cart page, handing it a notice the cart page knows.
   *
   * @param notice the notice the cart page says
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the cart's page
   */
  public static String redirectToCart(
      final CartNotice notice, final RedirectAttributes attributes) {
    notice.addTo(attributes);
    return "redirect:" + CART;
  }

  /**
   * The Spring MVC redirect to the cart page, handing it a notice the cart page knows and the
   * number it names.
   *
   * @param notice the notice the cart page says
   * @param count the number the notice names
   * @param attributes the redirect's attributes, which take the notice
   * @return {@code redirect:} followed by the cart's page
   */
  public static String redirectToCart(
      final CartNotice notice, final int count, final RedirectAttributes attributes) {
    notice.addTo(attributes, count);
    return "redirect:" + CART;
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
