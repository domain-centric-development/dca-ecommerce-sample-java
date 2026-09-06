package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getactivecheckoutsession.GetActiveCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions.GetShippingOptionsInputPort;
import dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions.GetShippingOptionsQuery;
import dev.domaincentric.sample.ecommerce.checkout.application.getshippingoptions.GetShippingOptionsResult;
import dev.domaincentric.sample.ecommerce.checkout.application.submitdelivery.SubmitDeliveryCommand;
import dev.domaincentric.sample.ecommerce.checkout.application.submitdelivery.SubmitDeliveryInputPort;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CustomerId;
import dev.domaincentric.sample.ecommerce.checkout.domain.service.CheckoutStepValidator;
import dev.domaincentric.sample.ecommerce.sharedkernel.application.shared.IdentityProvider;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC Controller for the delivery/shipping address page in checkout.
 *
 * <p>This controller handles the delivery step of checkout where the customer enters their shipping
 * address and selects a shipping option.
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
public class DeliveryPageController {

  private final GetCheckoutSessionInputPort getCheckoutSessionInputPort;
  private final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort;
  private final GetShippingOptionsInputPort getShippingOptionsInputPort;
  private final SubmitDeliveryInputPort submitDeliveryInputPort;
  private final IdentityProvider identityProvider;
  private final CheckoutStepValidator checkoutStepValidator;

  public DeliveryPageController(
      final GetCheckoutSessionInputPort getCheckoutSessionInputPort,
      final GetActiveCheckoutSessionInputPort getActiveCheckoutSessionInputPort,
      final GetShippingOptionsInputPort getShippingOptionsInputPort,
      final SubmitDeliveryInputPort submitDeliveryInputPort,
      final IdentityProvider identityProvider,
      final CheckoutStepValidator checkoutStepValidator) {
    this.getCheckoutSessionInputPort = getCheckoutSessionInputPort;
    this.getActiveCheckoutSessionInputPort = getActiveCheckoutSessionInputPort;
    this.getShippingOptionsInputPort = getShippingOptionsInputPort;
    this.submitDeliveryInputPort = submitDeliveryInputPort;
    this.identityProvider = identityProvider;
    this.checkoutStepValidator = checkoutStepValidator;
  }

  /**
   * Displays the delivery address and shipping options form.
   *
   * <p>This endpoint retrieves the active checkout session for the current user (via JWT identity)
   * and available shipping options, then displays the delivery form. If no active session is found,
   * redirects to cart with an error.
   *
   * @param model the Spring MVC model
   * @param redirectAttributes for passing flash messages on error
   * @return the delivery.pug template or redirect on error
   */
  @GetMapping("/delivery")
  public String showDeliveryForm(final Model model, final RedirectAttributes redirectAttributes) {

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
        checkoutStepValidator.validateStepAccess(result.session(), CheckoutStep.DELIVERY);
    if (redirect.isPresent()) {
      return "redirect:" + redirect.get();
    }

    // Convert to page-specific ViewModel
    final DeliveryPageViewModel viewModel = DeliveryPageViewModel.fromSnapshot(result.session());

    final GetShippingOptionsResult shippingOptions =
        getShippingOptionsInputPort.execute(GetShippingOptionsQuery.create());

    model.addAttribute("deliveryPage", viewModel);
    model.addAttribute("shippingOptions", shippingOptions.shippingOptions());
    model.addAttribute("title", "Delivery - Checkout");

    return "checkout/delivery";
  }

  /**
   * Submits delivery information and advances to payment step.
   *
   * <p>This endpoint processes the delivery form submission and redirects to the payment step on
   * success.
   *
   * @param street the street address
   * @param streetLine2 optional second address line
   * @param city the city
   * @param postalCode the postal code
   * @param country the country
   * @param state optional state/province
   * @param shippingOptionId the selected shipping option ID
   * @param shippingOptionName the shipping option display name
   * @param estimatedDelivery the estimated delivery time
   * @param shippingCost the shipping cost
   * @param currencyCode the currency code for shipping cost
   * @param redirectAttributes for passing flash messages
   * @return redirect to payment step or back to delivery on error
   */
  @PostMapping("/delivery")
  public String submitDelivery(
      @RequestParam final String street,
      @RequestParam(required = false) final String streetLine2,
      @RequestParam final String city,
      @RequestParam final String postalCode,
      @RequestParam final String country,
      @RequestParam(required = false) final String state,
      @RequestParam final String shippingOptionId,
      @RequestParam final String shippingOptionName,
      @RequestParam final String estimatedDelivery,
      @RequestParam final BigDecimal shippingCost,
      @RequestParam final String currencyCode,
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
      submitDeliveryInputPort.execute(
          new SubmitDeliveryCommand(
              activeSession.sessionId(),
              street,
              streetLine2,
              city,
              postalCode,
              country,
              state,
              shippingOptionId,
              shippingOptionName,
              estimatedDelivery,
              shippingCost,
              currencyCode));

      return "redirect:/checkout/payment";

    } catch (IllegalArgumentException | IllegalStateException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/checkout/delivery";
    }
  }
}
