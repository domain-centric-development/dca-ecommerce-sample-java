package dev.domaincentric.sample.ecommerce.cart.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class CartPageNoticeTest {

  @Test
  @DisplayName("a success notice is shown as the message, an error as the error")
  void aSuccessIsTheMessageAndAnErrorTheError() {
    final ExtendedModelMap model = new ExtendedModelMap();

    model.addAttribute(CartPageNotice.KEY, "welcome-back");
    CartPageNotice.show(model);
    assertEquals("Welcome back!", model.getAttribute("message"));

    model.addAttribute(CartPageNotice.KEY, "no-active-checkout");
    CartPageNotice.show(model);
    assertEquals("No active checkout session found", model.getAttribute("error"));
  }

  @Test
  @DisplayName("a notice that names numbers words them, and says nothing without all of them")
  void aNoticeThatNamesNumbersWordsThem() {
    assertEquals(
        Optional.of(
            "There is not enough stock to add 5 of this product — your cart already holds 5, and 8"
                + " are in stock."),
        CartPageNotice.NOT_ENOUGH_STOCK.sentence(List.of(5, 5, 8)));
    assertEquals(Optional.empty(), CartPageNotice.NOT_ENOUGH_STOCK.sentence(List.of(5)));
    assertEquals(Optional.empty(), CartPageNotice.NOT_ENOUGH_STOCK.sentence(List.of()));
  }

  @Test
  @DisplayName("the stock refusal the page shows names what was asked, held and in stock")
  void theStockRefusalShowsTheThreeNumbersTheRedirectCarried() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
    CartPageNotice.NOT_ENOUGH_STOCK.redirectTo("/cart", attributes, 1, 8, 8);
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAllAttributes(attributes.getFlashAttributes());

    CartPageNotice.show(model);

    assertEquals(
        "There is not enough stock to add 1 of this product — your cart already holds 8, and 8"
            + " are in stock.",
        model.getAttribute("error"));
  }

  @Test
  @DisplayName("an unknown or absent code, or a missing number, shows nothing")
  void anUnknownOrAbsentCodeShowsNothing() {
    final ExtendedModelMap model = new ExtendedModelMap();

    CartPageNotice.show(model);
    model.addAttribute(CartPageNotice.KEY, "<b>Pay here</b>");
    CartPageNotice.show(model);
    model.addAttribute(CartPageNotice.KEY, "not-enough-stock");
    CartPageNotice.show(model);

    assertFalse(model.containsAttribute("message"));
    assertFalse(model.containsAttribute("error"));
  }

  @Test
  @DisplayName("the redirect hands the notice to the page, the address stays as it is")
  void theRedirectHandsTheNoticeToThePage() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();
    assertEquals(
        "redirect:/cart/merge?anonymousUserId=a",
        CartPageNotice.INVALID_MERGE_OPTION.redirectTo(
            "/cart/merge?anonymousUserId=a", attributes));
    assertEquals(
        Map.of(CartPageNotice.KEY, "invalid-merge-option"), attributes.getFlashAttributes());

    final RedirectAttributesModelMap withCount = new RedirectAttributesModelMap();
    assertEquals(
        "redirect:/cart", CartPageNotice.NOT_ENOUGH_STOCK.redirectTo("/cart", 3, withCount));
    assertEquals(
        Map.of(CartPageNotice.KEY, "not-enough-stock", CartPageNotice.COUNT_KEY, 3),
        withCount.getFlashAttributes());
  }
}
