package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CartNotice;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutRoutes;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout.ResumeCheckoutCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout.ResumeCheckoutInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.resumecheckout.ResumeCheckoutResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitbuyerinfo.SubmitBuyerInfoInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
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
  private final ResumeCheckoutInputPort resumeCheckoutInputPort;
  private final IdentityProvider identityProvider;

  public BuyerInfoPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final SubmitBuyerInfoInputPort submitBuyerInfoInputPort,
      final ResumeCheckoutInputPort resumeCheckoutInputPort,
      final IdentityProvider identityProvider) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.submitBuyerInfoInputPort = submitBuyerInfoInputPort;
    this.resumeCheckoutInputPort = resumeCheckoutInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the buyer information form.
   *
   * <p>This endpoint retrieves the active checkout session for the current user (via JWT identity)
   * and displays the buyer info form. Without one, the open checkout of the user's cart is resumed;
   * if there is none either, redirects to cart with an error.
   *
   * @param model the Spring MVC model, holding the notice a redirect handed the page, if any
   * @param attributes the attributes of a redirect away from the page
   * @return the buyer.pug template or redirect on error
   */
  @GetMapping("/buyer")
  public String showBuyerInfoForm(final Model model, final RedirectAttributes attributes) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    final Optional<String> sessionId = activeOrResumedSessionId(customerId);

    if (sessionId.isEmpty()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_ACTIVE_CHECKOUT, attributes);
    }

    // Full session details, plus the domain's decision whether this step may be opened
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.forStep(sessionId.get(), customerId, CheckoutStep.BUYER_INFO));

    if (!result.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.CHECKOUT_NOT_FOUND, attributes);
    }
    final Optional<String> denied = CheckoutStepPage.redirectIfDenied(result.stepAccess(), model);
    if (denied.isPresent()) {
      return denied.get();
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
   * @return redirect to delivery step or back to buyer info on error
   */
  @PostMapping("/buyer")
  public String submitBuyerInfo(
      @RequestParam final String email,
      @RequestParam final String firstName,
      @RequestParam final String lastName,
      @RequestParam final String phone,
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

    return CheckoutStepPage.submitting(
        CheckoutStep.BUYER_INFO,
        () -> stepAccess(activeSession.sessionId(), customerId, CheckoutStep.BUYER_INFO),
        attributes,
        () -> {
          submitBuyerInfoInputPort.execute(
              new SubmitBuyerInfoCommand(
                  activeSession.sessionId(), customerId, email, firstName, lastName, phone));

          return "redirect:/checkout/delivery";
        });
  }

  /**
   * The customer's active session, or the open checkout of their cart they resume — the one place a
   * step page's GET changes a session (ADR-046): after a login the guest's checkout becomes theirs.
   */
  private Optional<String> activeOrResumedSessionId(final String customerId) {
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(GetActiveCheckoutSessionQuery.of(customerId));
    if (activeSession.found()) {
      return Optional.ofNullable(activeSession.sessionId());
    }
    final ResumeCheckoutResult resumed =
        resumeCheckoutInputPort.execute(new ResumeCheckoutCommand(customerId));
    return resumed.resumed() ? Optional.ofNullable(resumed.sessionId()) : Optional.empty();
  }

  /** The domain's decision whether the step may be opened, for a submission sent too early. */
  private StepAccess stepAccess(
      final String sessionId, final String customerId, final CheckoutStep step) {
    return getCheckoutSessionInputPort
        .execute(GetCheckoutSessionQuery.forStep(sessionId, customerId, step))
        .stepAccess();
  }
}
