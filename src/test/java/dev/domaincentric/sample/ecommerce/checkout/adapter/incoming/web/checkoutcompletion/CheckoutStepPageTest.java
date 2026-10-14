package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.checkoutcompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web.CheckoutStepNotice;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutSessionId;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStepOutOfOrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckoutStepPageTest {

  private static final CheckoutSessionId SESSION = CheckoutSessionId.generate();

  @Test
  @DisplayName("an accepted submission leads where it says")
  void anAcceptedSubmissionLeadsWhereItSays() {
    assertEquals(
        "redirect:/checkout/payment",
        CheckoutStepPage.submitting(CheckoutStep.DELIVERY, () -> "redirect:/checkout/payment"));
  }

  @Test
  @DisplayName("a refusal leads back to its step with the step's notice")
  void aRefusalLeadsBackToItsStepWithItsNotice() {
    assertEquals(
        "redirect:/checkout/delivery?notice=invalid-delivery-address",
        CheckoutStepPage.submitting(
            CheckoutStep.DELIVERY,
            () -> {
              throw new IllegalArgumentException("Street is required");
            }));
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
