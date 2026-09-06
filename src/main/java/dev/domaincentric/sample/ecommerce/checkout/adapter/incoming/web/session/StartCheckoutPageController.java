package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.application.startcheckout.StartCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.startcheckout.StartCheckoutInputPort;
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
   * @param redirectAttributes for passing flash messages on error
   * @return redirect to buyer info step at /checkout/buyer
   */
  @PostMapping("/start")
  public String startCheckout(
      @RequestParam final String cartId, final RedirectAttributes redirectAttributes) {

    try {
      startCheckoutInputPort.execute(new StartCheckoutCommand(cartId, currentCustomerId()));
      return "redirect:/checkout/buyer";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/cart";
    }
  }
}
