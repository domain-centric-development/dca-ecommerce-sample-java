package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepNotCompletedException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class CheckoutStepPageTest {

  private static final CheckoutSessionId SESSION = CheckoutSessionId.generate();

  private static final Supplier<StepAccess> NOT_ASKED =
      () -> {
        throw new AssertionError("step access asked for a refusal that did not come too early");
      };

  private final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

  @Test
  @DisplayName("an accepted submission leads where it says")
  void anAcceptedSubmissionLeadsWhereItSays() {
    assertEquals(
        "redirect:/checkout/payment",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY, NOT_ASKED, attributes, () -> "redirect:/checkout/payment"));
    assertTrue(attributes.getFlashAttributes().isEmpty());
  }

  @Test
  @DisplayName("a refusal leads back to its step with the step's notice")
  void aRefusalLeadsBackToItsStepWithItsNotice() {
    assertEquals(
        "redirect:/checkout/delivery",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY,
            NOT_ASKED,
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
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY,
            () -> StepAccess.redirectTo(CheckoutStep.BUYER_INFO),
            attributes,
            () -> {
              throw new CheckoutStepNotCompletedException(SESSION, CheckoutStep.BUYER_INFO);
            }));
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "earlier-steps-first"), attributes.getFlashAttributes());
  }

  @Test
  @DisplayName("a step sent too early whose checkout is gone leads to the cart, saying nothing")
  void aStepSentTooEarlyWithoutACheckoutLeadsToTheCart() {
    assertEquals(
        "redirect:/cart",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY,
            StepAccess::backToCart,
            attributes,
            () -> {
              throw new CheckoutStepNotCompletedException(SESSION, CheckoutStep.BUYER_INFO);
            }));
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
    final CheckoutStepOutOfOrderException outOfOrder =
        new CheckoutStepOutOfOrderException(
            SESSION, CheckoutStep.CONFIRMATION, CheckoutStep.PAYMENT);

    assertEquals(
        CheckoutStepNotice.REVIEW_FIRST,
        CheckoutStepPage.noticeFor(CheckoutStep.REVIEW, outOfOrder));
    assertEquals(
        CheckoutStepNotice.EARLIER_STEPS_FIRST,
        CheckoutStepPage.noticeFor(CheckoutStep.PAYMENT, outOfOrder));
  }
}
