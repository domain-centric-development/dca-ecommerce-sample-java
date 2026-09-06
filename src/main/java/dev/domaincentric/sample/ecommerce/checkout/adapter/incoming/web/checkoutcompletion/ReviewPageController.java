package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutStepValidator;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the order review page in checkout.
 *
 * <p>This controller handles the review step of checkout where the customer reviews all order
 * details before confirming the purchase.
 *
 * <p>The active checkout session is identified via JWT identity, removing the need for session IDs
 * in URLs.
 *
 * <p><b>Clean Architecture:</b> This controller depends on use case interfaces (input ports)
 * instead of application services, following the Dependency Inversion Principle.
 *
 * <p><b>Naming Convention:</b> MVC controllers use {@code @Controller} annotation and end with
 * "Controller" suffix.
 */
@Controller
@RequestMapping("/checkout")
public class ReviewPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final IdentityProvider identityProvider;
  private final CheckoutStepValidator checkoutStepValidator;

  public ReviewPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final IdentityProvider identityProvider,
      final CheckoutStepValidator checkoutStepValidator) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.identityProvider = identityProvider;
    this.checkoutStepValidator = checkoutStepValidator;
  }

  /**
   * Displays the order review page with all order details.
   *
   * <p>This endpoint retrieves the active checkout session for the current user (via JWT identity)
   * and displays all collected information including buyer info, delivery details, payment method,
   * and order summary for final review before purchase confirmation.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the review.pug template or redirect on error
   */
  @GetMapping("/review")
  public String showReviewPage(final Model model, final RedirectAttributes redirectAttributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final CustomerId customerId = CustomerId.of(identity.userId().value());

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(
            GetActiveCheckoutSessionQuery.of(customerId.value()));

    if (!activeSession.found()) {
      redirectAttributes.addFlashAttribute("error", "No active checkout session found");
      return "redirect:/cart";
    }

    // Get full session details
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(GetCheckoutSessionQuery.of(activeSession.sessionId()));

    if (!result.found()) {
      redirectAttributes.addFlashAttribute("error", "Checkout session not found");
      return "redirect:/cart";
    }

    // The domain decides whether this step may be opened at all
    final Optional<String> redirect =
        checkoutStepValidator.validateStepAccess(result.session(), CheckoutStep.REVIEW);
    if (redirect.isPresent()) {
      return "redirect:" + redirect.get();
    }

    // Convert to page-specific ViewModel
    final ReviewPageViewModel viewModel = ReviewPageViewModel.fromSnapshot(result.session());

    model.addAttribute("orderReview", viewModel);
    model.addAttribute("title", "Review Order - Checkout");

    return "checkout/review";
  }
}
