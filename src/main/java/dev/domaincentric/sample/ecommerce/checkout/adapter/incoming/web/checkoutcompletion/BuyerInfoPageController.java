package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutStepValidator;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the buyer information page in checkout.
 *
 * <p>This controller handles the buyer info step of checkout where the customer enters their
 * contact information (email, name, phone).
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
public class BuyerInfoPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final SubmitBuyerInfoInputPort submitBuyerInfoInputPort;
  private final IdentityProvider identityProvider;
  private final CheckoutStepValidator checkoutStepValidator;

  public BuyerInfoPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final SubmitBuyerInfoInputPort submitBuyerInfoInputPort,
      final IdentityProvider identityProvider,
      final CheckoutStepValidator checkoutStepValidator) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.submitBuyerInfoInputPort = submitBuyerInfoInputPort;
    this.identityProvider = identityProvider;
    this.checkoutStepValidator = checkoutStepValidator;
  }

  /**
   * Displays the buyer information form.
   *
   * <p>This endpoint retrieves the active checkout session for the current user (via JWT identity)
   * and displays the buyer info form. If no active session is found, redirects to cart with an
   * error.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the buyer.pug template or redirect on error
   */
  @GetMapping("/buyer")
  public String showBuyerInfoForm(final Model model, final RedirectAttributes redirectAttributes) {

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
        checkoutStepValidator.validateStepAccess(result.session(), CheckoutStep.BUYER_INFO);
    if (redirect.isPresent()) {
      return "redirect:" + redirect.get();
    }

    // Convert to page-specific ViewModel
    final BuyerInfoPageViewModel viewModel = BuyerInfoPageViewModel.fromSnapshot(result.session());

    model.addAttribute("buyerInfoPage", viewModel);
    model.addAttribute("identity", identity);
    model.addAttribute("title", "Buyer Information - Checkout");

    return "checkout/buyer";
  }

  /**
   * Submits buyer information and advances to delivery step.
   *
   * <p>This endpoint processes the buyer info form submission and redirects to the delivery step on
   * success.
   *
   * @param email the buyer's email address
   * @param firstName the buyer's first name
   * @param lastName the buyer's last name
   * @param phone the buyer's phone number
   * @param redirectAttributes for passing flash messages
   * @return redirect to delivery step or back to buyer info on error
   */
  @PostMapping("/buyer")
  public String submitBuyerInfo(
      @RequestParam final String email,
      @RequestParam final String firstName,
      @RequestParam final String lastName,
      @RequestParam final String phone,
      final RedirectAttributes redirectAttributes) {

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

    try {
      submitBuyerInfoInputPort.execute(
          new SubmitBuyerInfoCommand(activeSession.sessionId(), email, firstName, lastName, phone));

      return "redirect:/checkout/delivery";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/buyer";
    }
  }
}
