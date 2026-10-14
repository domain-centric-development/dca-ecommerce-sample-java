package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckoutStepNoticeTest {

  @Test
  @DisplayName("a known code is its notice")
  void aKnownCodeIsItsNotice() {
    assertEquals(
        Optional.of(CheckoutStepNotice.EARLIER_STEPS_FIRST),
        CheckoutStepNotice.fromCode("earlier-steps-first"));
    assertEquals(
        Optional.of("Please complete the previous checkout steps first."),
        CheckoutStepNotice.EARLIER_STEPS_FIRST.sentence(null));
  }

  @Test
  @DisplayName("a notice that names a number words it, and says nothing without one")
  void aNoticeThatNamesANumberWordsIt() {
    assertEquals(
        Optional.of("Cannot place the order, 2 item(s) unavailable or out of stock."),
        CheckoutStepNotice.ITEMS_UNAVAILABLE.sentence(2));
    assertEquals(Optional.empty(), CheckoutStepNotice.ITEMS_UNAVAILABLE.sentence(null));
  }

  @Test
  @DisplayName("an unknown or absent code is no notice")
  void anUnknownOrAbsentCodeIsNoNotice() {
    assertEquals(Optional.empty(), CheckoutStepNotice.fromCode("<b>Pay here</b>"));
    assertEquals(Optional.empty(), CheckoutStepNotice.fromCode(null));
  }
}
