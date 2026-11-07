package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.application.session.getcheckoutsession.GetCheckoutSessionResult;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class CheckoutStepPageTest {

  private static final CheckoutSessionId SESSION = CheckoutSessionId.generate();

  private final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

  @Test
  @DisplayName("an accepted submission leads where it says")
  void anAcceptedSubmissionLeadsWhereItSays() {
    assertEquals(
        "redirect:/checkout/payment",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY, attributes, () -> "redirect:/checkout/payment"));
    assertTrue(attributes.getFlashAttributes().isEmpty());
  }

  @Test
  @DisplayName("a refusal leads back to its step with the step's notice")
  void aRefusalLeadsBackToItsStepWithItsNotice() {
    assertEquals(
        "redirect:/checkout/delivery",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY,
            attributes,
            () -> {
              throw new IllegalArgumentException("Street is required");
            }));
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "invalid-delivery-address"),
        attributes.getFlashAttributes());
  }

  @Test
  @DisplayName("a step sent too early leads to the step still open, which says why")
  void aStepSentTooEarlyLeadsToTheStepStillOpen() {
    assertEquals(
        "redirect:/checkout/buyer",
        CheckoutStepPage.stepNotCompleted(
            CheckoutStep.DELIVERY,
            () -> StepAccess.redirectTo(CheckoutStep.BUYER_INFO),
            attributes));
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "earlier-steps-first"), attributes.getFlashAttributes());
  }

  @Test
  @DisplayName("a step sent too early whose checkout is gone leads to the cart, saying nothing")
  void aStepSentTooEarlyWithoutACheckoutLeadsToTheCart() {
    assertEquals(
        "redirect:/cart",
        CheckoutStepPage.stepNotCompleted(
            CheckoutStep.DELIVERY, StepAccess::backToCart, attributes));
    assertTrue(attributes.getFlashAttributes().isEmpty());
  }

  @Test
  @DisplayName("a denied step passes no notice on")
  void aDeniedStepPassesNoNoticeOn() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(CheckoutStepNotice.KEY, "earlier-steps-first");

    assertEquals(
        Optional.of("redirect:/checkout/buyer"),
        CheckoutStepPage.redirectIfDenied(StepAccess.redirectTo(CheckoutStep.BUYER_INFO), model));
    assertFalse(model.containsAttribute("error"));
  }

  @Test
  @DisplayName("a granted step shows the notice a redirect handed it")
  void aGrantedStepShowsItsNotice() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(CheckoutStepNotice.KEY, "items-unavailable");
    model.addAttribute(CheckoutStepNotice.COUNT_KEY, 2);

    assertEquals(Optional.empty(), CheckoutStepPage.redirectIfDenied(StepAccess.grant(), model));
    assertEquals(
        "Cannot place the order, 2 item(s) unavailable or out of stock.",
        model.getAttribute("error"));
  }

  @Test
  @DisplayName(
      "an order placed out of order asks for the review, any other step for the earlier steps")
  void anOutOfOrderStepNamesWhatComesFirst() {
    final RedirectAttributesModelMap review = new RedirectAttributesModelMap();
    final RedirectAttributesModelMap payment = new RedirectAttributesModelMap();

    CheckoutStepPage.stepOutOfOrder(CheckoutStep.REVIEW, StepAccess::grant, review);
    CheckoutStepPage.stepOutOfOrder(CheckoutStep.PAYMENT, StepAccess::grant, payment);

    assertEquals(Map.of(CheckoutStepNotice.KEY, "review-first"), review.getFlashAttributes());
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "earlier-steps-first"), payment.getFlashAttributes());
  }

  @Test
  @DisplayName("a step asked for a checkout the customer does not have sends them to the cart")
  void aStepWithoutACheckoutSendsTheCustomerToTheCart() {
    assertTrue(
        CheckoutStepPage.stepAccessOf(new GetCheckoutSessionResult.CheckoutSessionNotFound(SESSION))
            .isBackToCart());
  }
}
