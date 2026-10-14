package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CartNotice;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutRoutes;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.confirmcheckout.ConfirmCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getconfirmedcheckoutsession.GetConfirmedCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * MVC Controller for order confirmation in checkout.
 *
 * <p>This controller handles the confirmation step of checkout where the customer confirms their
 * order and views the thank you page.
 *
 * <p>The checkout session is identified via JWT identity, removing the need for session IDs in
 * URLs.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and end with
 * "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class ConfirmationPageController {

  private final ConfirmCheckoutInputPort confirmCheckoutInputPort;
  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final GetConfirmedCheckoutSessionInputPort getConfirmedCheckoutSessionInputPort;
  private final IdentityProvider identityProvider;

  public ConfirmationPageController(
      final ConfirmCheckoutInputPort confirmCheckoutInputPort,
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final GetConfirmedCheckoutSessionInputPort getConfirmedCheckoutSessionInputPort,
      final IdentityProvider identityProvider) {
    this.confirmCheckoutInputPort = confirmCheckoutInputPort;
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.getConfirmedCheckoutSessionInputPort = getConfirmedCheckoutSessionInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Confirms the checkout and places the order.
   *
   * <p>This endpoint finds the active checkout session for the current user (via JWT identity),
   * processes the order confirmation, and redirects to the confirmation page on success.
   *
   * @return redirect to confirmation page or back to review on error
   */
  @PostMapping("/confirm")
  public String confirmOrder() {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(GetActiveCheckoutSessionQuery.of(customerId));

    if (!activeSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_ACTIVE_CHECKOUT);
    }

    return CheckoutStepPage.submitting(
        CheckoutStep.REVIEW,
        () -> {
          confirmCheckoutInputPort.execute(
              new ConfirmCheckoutCommand(activeSession.sessionId(), customerId));

          return "redirect:/checkout/confirmation";
        });
  }

  /**
   * Displays the order confirmation (thank you) page.
   *
   * <p>This endpoint shows the confirmation page after a successful order. It looks up the
   * confirmed/completed session for the current user (via JWT identity). It is only accessible
   * after the checkout has been confirmed.
   *
   * @param model the Spring MVC model
   * @return the confirmation.pug template or redirect on error
   */
  @GetMapping("/confirmation")
  public String showConfirmationPage(final Model model) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find confirmed or completed checkout session for the user
    final GetConfirmedCheckoutSessionResult confirmedSession =
        getConfirmedCheckoutSessionInputPort.execute(
            GetConfirmedCheckoutSessionQuery.of(customerId));

    if (!confirmedSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_CONFIRMED_ORDER);
    }

    // Full session details, plus the domain's decision whether this step may be opened
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.forStep(
                confirmedSession.sessionId(), customerId, CheckoutStep.CONFIRMATION));

    if (!result.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.CHECKOUT_NOT_FOUND);
    }
    if (!result.stepAccess().granted()) {
      return CheckoutRoutes.redirectFor(
          result.stepAccess(), CheckoutStepNotice.ORDER_NOT_CONFIRMED);
    }

    // Convert to page-specific ViewModel
    final ConfirmationPageViewModel viewModel =
        ConfirmationPageViewModel.fromSnapshot(result.session());

    model.addAttribute("orderConfirmation", viewModel);
    model.addAttribute("title", "Order Confirmed - Thank You!");

    return "checkout/confirmation";
  }
}
