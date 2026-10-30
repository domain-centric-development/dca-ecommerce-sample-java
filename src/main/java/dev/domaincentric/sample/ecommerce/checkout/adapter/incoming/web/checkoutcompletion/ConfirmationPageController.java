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
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the confirmation step of checkout.
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

  /** The value of {@code sentFrom} the review's own form carries. */
  private static final String REVIEW_PAGE = "review";

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
   * places the order, and redirects to the confirmation page on success. An order is placed only
   * from the review page, the last check before it: a "Place Order" sent from anywhere else leads
   * to the review, asking for it — or, while an earlier step is still missing, to that step.
   *
   * @param sentFrom the page the form was sent from, {@code review} from the review's own form
   * @param reviewFingerprint the fingerprint of the review the form was sent from; a form without
   *     one matches no review
   * @param attributes the attributes of the redirect
   * @return redirect to confirmation page, or to the review or the step still open on refusal
   */
  @PostMapping("/confirm")
  public String confirmOrder(
      @RequestParam(name = "sentFrom", required = false) final @Nullable String sentFrom,
      @RequestParam(name = "reviewFingerprint", defaultValue = "") final String reviewFingerprint,
      final RedirectAttributes attributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(GetActiveCheckoutSessionQuery.of(customerId));

    if (!activeSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_ACTIVE_CHECKOUT, attributes);
    }

    if (!REVIEW_PAGE.equals(sentFrom)) {
      return redirectToTheReview(activeSession.sessionId(), customerId, attributes);
    }

    return CheckoutStepPage.submitting(
        CheckoutStep.REVIEW,
        () -> stepAccess(activeSession.sessionId(), customerId, CheckoutStep.REVIEW),
        attributes,
        () -> {
          confirmCheckoutInputPort.execute(
              new ConfirmCheckoutCommand(activeSession.sessionId(), customerId, reviewFingerprint));

          return "redirect:/checkout/confirmation";
        });
  }

  /**
   * A "Place Order" not sent from the review: the review, asking for it — or, while an earlier step
   * is still missing, that step.
   */
  private String redirectToTheReview(
      final String sessionId, final String customerId, final RedirectAttributes attributes) {
    final StepAccess review = stepAccess(sessionId, customerId, CheckoutStep.REVIEW);
    return review.granted()
        ? CheckoutRoutes.redirectTo(
            CheckoutStep.REVIEW, CheckoutStepNotice.REVIEW_FIRST, attributes)
        : CheckoutRoutes.redirectFor(review, CheckoutStepNotice.EARLIER_STEPS_FIRST, attributes);
  }

  /**
   * Displays the confirmation (thank you) page of a placed order.
   *
   * <p>This endpoint shows the confirmation page after a successful order. It looks up the
   * confirmed/completed session for the current user (via JWT identity). It is only accessible
   * after the checkout has been confirmed.
   *
   * @param model the Spring MVC model
   * @param attributes the attributes of a redirect away from the page
   * @return the confirmation.pug template or redirect on error
   */
  @GetMapping("/confirmation")
  public String showConfirmationPage(final Model model, final RedirectAttributes attributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find confirmed or completed checkout session for the user
    final GetConfirmedCheckoutSessionResult confirmedSession =
        getConfirmedCheckoutSessionInputPort.execute(
            GetConfirmedCheckoutSessionQuery.of(customerId));

    if (!confirmedSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_CONFIRMED_ORDER, attributes);
    }

    // Full session details, plus the domain's decision whether this step may be opened
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.forStep(
                confirmedSession.sessionId(), customerId, CheckoutStep.CONFIRMATION));

    if (!result.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.CHECKOUT_NOT_FOUND, attributes);
    }
    if (!result.stepAccess().granted()) {
      // Its own refusal, never one handed on. Not reachable today: the session looked up above is
      // confirmed or completed, and the domain grants either the confirmation.
      return CheckoutRoutes.redirectFor(
          result.stepAccess(), CheckoutStepNotice.ORDER_NOT_CONFIRMED, attributes);
    }

    // Convert to page-specific ViewModel
    final ConfirmationPageViewModel viewModel =
        ConfirmationPageViewModel.fromSnapshot(result.session());

    model.addAttribute("confirmedCheckout", viewModel);
    model.addAttribute("title", "Order Confirmed - Thank You!");

    return "checkout/confirmation";
  }

  /**
   * The domain's decision whether the step may be opened, for a submission sent too early or not
   * from the review.
   */
  private StepAccess stepAccess(
      final String sessionId, final String customerId, final CheckoutStep step) {
    return getCheckoutSessionInputPort
        .execute(GetCheckoutSessionQuery.forStep(sessionId, customerId, step))
        .stepAccess();
  }
}
