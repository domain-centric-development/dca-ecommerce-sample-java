package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.session;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CartNotice;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutRoutes;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.session.startcheckout.StartCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.ArticleNotAvailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartNotActiveException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CartNotAvailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutItemsUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.EmptyCartException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.EmptyCheckoutException;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for initiating checkout from a cart.
 *
 * <p>This controller handles the checkout initiation flow by:
 *
 * <ul>
 *   <li>Accepting the cart ID as a request parameter (passed from cart UI)
 *   <li>Starting a new checkout session via the use case
 *   <li>Redirecting to the buyer info step
 * </ul>
 *
 * <p><b>Clean Architecture:</b> This controller only depends on use case interfaces from its own
 * bounded context (checkout), avoiding cross-context dependencies. The cart ID is passed from the
 * UI, which already has this information.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and end with
 * "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class StartCheckoutPageController {

  private final StartCheckoutInputPort startCheckoutInputPort;
  private final IdentityProvider identityProvider;

  public StartCheckoutPageController(
      final StartCheckoutInputPort startCheckoutInputPort,
      final IdentityProvider identityProvider) {
    this.startCheckoutInputPort = startCheckoutInputPort;
    this.identityProvider = identityProvider;
  }

  private String currentCustomerId() {
    return identityProvider.getCurrentIdentity().userId().value();
  }

  /**
   * Initiates checkout from the specified cart.
   *
   * <p>The cart ID is posted from the cart page's checkout form, avoiding the need to query the
   * cart context directly. POST because starting a checkout creates a session — a state change that
   * must not be reachable through a link (prefetching, crawlers, CSRF via GET).
   *
   * @param cartId the cart ID to start checkout from
   * @param attributes the redirect's attributes, which take the cart page's notice on a refusal
   * @return redirect to buyer info step at /checkout/buyer
   */
  @PostMapping("/start")
  public String startCheckout(
      @RequestParam final String cartId, final RedirectAttributes attributes) {

    try {
      startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, currentCustomerId()));
      return "redirect:/checkout/buyer";

    } catch (UseCaseException | DomainException | IllegalArgumentException e) {
      // The two base types are the checkout's own refusals. IllegalArgumentException is still
      // here because a form field the customer typed reaches a value object unvalidated; each
      // field moved into request validation is one reason less to catch it.
      return redirectFor(e, attributes);
    }
  }

  /**
   * The cart page with the notice for a refusal: chosen by the failure's type, the number it names
   * taken from its values. The failure's message is for the log, never for the customer.
   */
  private static String redirectFor(
      final RuntimeException failure, final RedirectAttributes attributes) {
    return switch (failure) {
      case CartNotAvailableException e ->
          CheckoutRoutes.redirectToCart(CartNotice.CART_NOT_FOUND, attributes);
      case CartNotActiveException e ->
          CheckoutRoutes.redirectToCart(CartNotice.CART_NOT_CHECKABLE, attributes);
      case EmptyCartException e -> CheckoutRoutes.redirectToCart(CartNotice.EMPTY_CART, attributes);
      case EmptyCheckoutException e ->
          CheckoutRoutes.redirectToCart(CartNotice.EMPTY_CART, attributes);
      case ArticleNotAvailableException e ->
          CheckoutRoutes.redirectToCart(CartNotice.CART_PRODUCT_UNAVAILABLE, attributes);
      case CheckoutItemsUnavailableException e ->
          CheckoutRoutes.redirectToCart(
              CartNotice.ITEMS_UNAVAILABLE, e.productIds().size(), attributes);
      default -> CheckoutRoutes.redirectToCart(CartNotice.CHECKOUT_NOT_STARTED, attributes);
    };
  }
}
