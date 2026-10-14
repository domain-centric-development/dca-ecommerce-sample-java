package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class CheckoutRoutesTest {

  @ParameterizedTest
  @CsvSource({
    "BUYER_INFO, /checkout/buyer",
    "DELIVERY, /checkout/delivery",
    "PAYMENT, /checkout/payment",
    "REVIEW, /checkout/review",
    "CONFIRMATION, /checkout/confirmation"
  })
  @DisplayName("every step has a page")
  void everyStepHasAPage(final CheckoutStep step, final String path) {
    assertEquals(path, CheckoutRoutes.pathOf(step));
    assertEquals("redirect:" + path, CheckoutRoutes.redirectFor(StepAccess.redirectTo(step)));
  }

  @Test
  @DisplayName("back to cart redirects to the cart page")
  void backToCartRedirectsToTheCart() {
    assertEquals("redirect:/cart", CheckoutRoutes.redirectFor(StepAccess.backToCart()));
  }

  @Test
  @DisplayName("a granted access has no redirect")
  void grantedAccessHasNoRedirect() {
    assertThrows(
        IllegalArgumentException.class, () -> CheckoutRoutes.redirectFor(StepAccess.grant()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            CheckoutRoutes.redirectFor(
                StepAccess.grant(),
                CheckoutStepNotice.EARLIER_STEPS_FIRST,
                new RedirectAttributesModelMap()));
  }

  @Test
  @DisplayName("a denied step hands its own notice to the step shown instead, not in the address")
  void aDeniedStepHandsTheNoticeToTheStepShownInstead() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

    assertEquals(
        "redirect:/checkout/buyer",
        CheckoutRoutes.redirectFor(
            StepAccess.redirectTo(CheckoutStep.BUYER_INFO),
            CheckoutStepNotice.ORDER_NOT_CONFIRMED,
            attributes));
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "order-not-confirmed"), attributes.getFlashAttributes());
  }

  @Test
  @DisplayName("back to cart hands on no step notice")
  void backToCartHandsOnNoNotice() {
    // The confirmation's "Order has not been confirmed yet" would arrive here without a sentence,
    // as the cart page has no code for it. Not reachable today: the confirmation page looks up a
    // confirmed or completed session only, and the domain grants either the confirmation.
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

    assertEquals(
        "redirect:/cart",
        CheckoutRoutes.redirectFor(
            StepAccess.backToCart(), CheckoutStepNotice.ORDER_NOT_CONFIRMED, attributes));
    assertTrue(attributes.getFlashAttributes().isEmpty());
  }

  @Test
  @DisplayName("a redirect to a step hands it the notice and the number it names")
  void aRedirectToAStepHandsItTheNotice() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

    assertEquals(
        "redirect:/checkout/review",
        CheckoutRoutes.redirectTo(
            CheckoutStep.REVIEW, CheckoutStepNotice.ITEMS_UNAVAILABLE, 2, attributes));
    assertEquals(
        Map.of(CheckoutStepNotice.KEY, "items-unavailable", CheckoutStepNotice.COUNT_KEY, 2),
        attributes.getFlashAttributes());
  }

  @Test
  @DisplayName("a redirect to the cart hands it a code the cart page reads")
  void aRedirectToTheCartHandsItACartCode() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

    assertEquals(
        "redirect:/cart",
        CheckoutRoutes.redirectToCart(CartNotice.ITEMS_UNAVAILABLE, 3, attributes));
    assertEquals(
        Map.of(CartNotice.KEY, "items-unavailable", CartNotice.COUNT_KEY, 3),
        attributes.getFlashAttributes());
  }
}
