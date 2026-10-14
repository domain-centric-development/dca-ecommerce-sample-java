package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

class CartPageNoticeTest {

  @Test
  @DisplayName("a success notice is shown as the message, an error as the error")
  void aSuccessIsTheMessageAndAnErrorTheError() {
    final ExtendedModelMap model = new ExtendedModelMap();

    CartPageNotice.show(model, "welcome-back", null);
    assertEquals("Welcome back!", model.getAttribute("message"));

    CartPageNotice.show(model, "no-active-checkout", null);
    assertEquals("No active checkout session found", model.getAttribute("error"));
  }

  @Test
  @DisplayName("a notice that names a number words it, and says nothing without one")
  void aNoticeThatNamesANumberWordsIt() {
    assertEquals(
        Optional.of("There is not enough stock to add 3 of this product."),
        CartPageNotice.NOT_ENOUGH_STOCK.sentence(3));
    assertEquals(Optional.empty(), CartPageNotice.NOT_ENOUGH_STOCK.sentence(null));
  }

  @Test
  @DisplayName("an unknown or absent code shows nothing")
  void anUnknownOrAbsentCodeShowsNothing() {
    final ExtendedModelMap model = new ExtendedModelMap();

    CartPageNotice.show(model, "<b>Pay here</b>", null);
    CartPageNotice.show(model, null, null);

    assertFalse(model.containsAttribute("message"));
    assertFalse(model.containsAttribute("error"));
  }

  @Test
  @DisplayName("the redirect keeps the query the path already has")
  void theRedirectKeepsTheQuery() {
    assertEquals(
        "redirect:/cart?notice=product-added", CartPageNotice.PRODUCT_ADDED.redirectTo("/cart"));
    assertEquals(
        "redirect:/cart/merge?anonymousUserId=a&notice=invalid-merge-option",
        CartPageNotice.INVALID_MERGE_OPTION.redirectTo("/cart/merge?anonymousUserId=a"));
    assertEquals(
        "redirect:/cart?notice=not-enough-stock&count=3",
        CartPageNotice.NOT_ENOUGH_STOCK.redirectTo("/cart", 3));
  }
}
