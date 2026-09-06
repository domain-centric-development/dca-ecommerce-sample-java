package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.domaincentric.sample.ecommerce.checkout.domain.model.CheckoutStep;
import dev.domaincentric.sample.ecommerce.checkout.domain.model.StepAccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
  }
}
