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
        "Please complete the previous checkout steps first.",
        CheckoutStepNotice.EARLIER_STEPS_FIRST.sentence());
  }

  @Test
  @DisplayName("an unknown or absent code is no notice")
  void anUnknownOrAbsentCodeIsNoNotice() {
    assertEquals(Optional.empty(), CheckoutStepNotice.fromCode("<b>Pay here</b>"));
    assertEquals(Optional.empty(), CheckoutStepNotice.fromCode(null));
  }
}
