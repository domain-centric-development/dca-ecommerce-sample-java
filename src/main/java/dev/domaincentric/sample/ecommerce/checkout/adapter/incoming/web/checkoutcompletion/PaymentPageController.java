package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import dev.domaincentric.dca.buildingblocks.application.UseCaseException;
import dev.domaincentric.dca.buildingblocks.ddd.tactical.DomainException;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CartNotice;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutRoutes;
import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getpaymentproviders.GetPaymentProvidersInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getpaymentproviders.GetPaymentProvidersQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.getpaymentproviders.GetPaymentProvidersResult;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.checkoutcompletion.submitpayment.SubmitPaymentInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.CheckoutSessionNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentInitiationFailedException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderNotFoundException;
import dev.domaincentric.sample.ecommerce.checkout.application.shared.PaymentProviderUnavailableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutNotModifiableException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.NothingToPayException;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * MVC Controller for the payment method page in checkout.
 *
 * <p>This controller handles the payment step of checkout where the customer selects their payment
 * method.
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
public class PaymentPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final GetPaymentProvidersInputPort getPaymentProvidersInputPort;
  private final SubmitPaymentInputPort submitPaymentInputPort;
  private final IdentityProvider identityProvider;

  public PaymentPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final GetPaymentProvidersInputPort getPaymentProvidersInputPort,
      final SubmitPaymentInputPort submitPaymentInputPort,
      final IdentityProvider identityProvider) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.getPaymentProvidersInputPort = getPaymentProvidersInputPort;
    this.submitPaymentInputPort = submitPaymentInputPort;
    this.identityProvider = identityProvider;
  }

  /**
   * Displays the payment method selection form.
   *
   * <p>This endpoint retrieves the active checkout session for the current user (via JWT identity)
   * and available payment providers, then displays the payment form. If no active session is found,
   * redirects to cart with an error.
   *
   * @param noticeCode the code of the notice a redirect brought along, if any
   * @param model the Spring MVC model
   * @return the payment.pug template or redirect on error
   */
  @GetMapping("/payment")
  public String showPaymentForm(
      @RequestParam(name = CheckoutStepNotice.PARAMETER, required = false)
          final @Nullable String noticeCode,
      final Model model) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(GetActiveCheckoutSessionQuery.of(customerId));

    if (!activeSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_ACTIVE_CHECKOUT);
    }

    // Full session details, plus the domain's decision whether this step may be opened
    final GetCheckoutSessionResult result =
        getCheckoutSessionInputPort.execute(
            GetCheckoutSessionQuery.forStep(
                activeSession.sessionId(), customerId, CheckoutStep.PAYMENT));

    if (!result.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.CHECKOUT_NOT_FOUND);
    }
    final Optional<CheckoutStepNotice> notice = CheckoutStepNotice.fromCode(noticeCode);
    if (!result.stepAccess().granted()) {
      return notice
          .map(n -> CheckoutRoutes.redirectFor(result.stepAccess(), n))
          .orElseGet(() -> CheckoutRoutes.redirectFor(result.stepAccess()));
    }
    notice.flatMap(n -> n.sentence(null)).ifPresent(text -> model.addAttribute("error", text));

    // Convert to page-specific ViewModel
    final PaymentPageViewModel viewModel = PaymentPageViewModel.fromSnapshot(result.session());

    final GetPaymentProvidersResult paymentProviders =
        getPaymentProvidersInputPort.execute(GetPaymentProvidersQuery.create());

    model.addAttribute("paymentPage", viewModel);
    model.addAttribute("paymentProviders", paymentProviders.paymentProviders());
    model.addAttribute("title", "Payment - Checkout");

    return "checkout/payment";
  }

  /**
   * Submits payment method selection and advances to review step.
   *
   * <p>This endpoint processes the payment method selection and redirects to the review step on
   * success.
   *
   * @param providerId the selected payment provider ID
   * @return redirect to review step or back to payment on error
   */
  @PostMapping("/payment")
  public String submitPayment(@RequestParam final String providerId) {

    // Get customer ID from JWT identity
    final IdentityProvider.Identity identity = identityProvider.getCurrentIdentity();
    final String customerId = identity.userId().value();

    // Find active checkout session for the user
    final GetActiveCheckoutSessionResult activeSession =
        getActiveCheckoutSessionInputPort.execute(GetActiveCheckoutSessionQuery.of(customerId));

    if (!activeSession.found()) {
      return CheckoutRoutes.redirectToCart(CartNotice.NO_ACTIVE_CHECKOUT);
    }

    try {
      submitPaymentInputPort.execute(
          new SubmitPaymentCommand(activeSession.sessionId(), customerId, providerId));

      return "redirect:/checkout/review";

    } catch (CheckoutStepNotCompletedException | CheckoutStepOutOfOrderException e) {
      // The step shown instead says it; the redirect carries it to whichever instance answers
      return CheckoutRoutes.redirectTo(
          CheckoutStep.PAYMENT, CheckoutStepNotice.EARLIER_STEPS_FIRST);
    } catch (UseCaseException | DomainException | IllegalArgumentException e) {
      // The two base types are the checkout's own refusals. IllegalArgumentException is still
      // here because a form field the customer typed reaches a value object unvalidated; each
      // field moved into request validation is one reason less to catch it.
      return CheckoutRoutes.redirectTo(CheckoutStep.PAYMENT, noticeFor(e));
    }
  }

  /**
   * The notice for a refusal: chosen by the failure's type. The failure's message is for the log,
   * never for the customer.
   */
  private static CheckoutStepNotice noticeFor(final RuntimeException failure) {
    return switch (failure) {
      case PaymentInitiationFailedException e -> CheckoutStepNotice.PAYMENT_REFUSED;
      case PaymentProviderUnavailableException e -> CheckoutStepNotice.PAYMENT_PROVIDER_UNAVAILABLE;
      case PaymentProviderNotFoundException e -> CheckoutStepNotice.PAYMENT_METHOD_NOT_OFFERED;
      case NothingToPayException e -> CheckoutStepNotice.NOTHING_TO_PAY;
      case CheckoutSessionNotFoundException e -> CheckoutStepNotice.SESSION_NOT_FOUND;
      case CheckoutNotModifiableException e -> CheckoutStepNotice.CHECKOUT_NOT_MODIFIABLE;
      case IllegalArgumentException e -> CheckoutStepNotice.PAYMENT_METHOD_MISSING;
      default -> CheckoutStepNotice.PAYMENT_NOT_SAVED;
    };
  }
}
