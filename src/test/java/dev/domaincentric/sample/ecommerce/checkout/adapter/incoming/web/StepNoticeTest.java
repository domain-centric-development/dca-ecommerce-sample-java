package dev.domaincentric.sample.ecommerce.checkout.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StepNoticeTest {

  @Test
  @DisplayName("a known code is its notice")
  void aKnownCodeIsItsNotice() {
    assertEquals(
        Optional.of(StepNotice.EARLIER_STEPS_FIRST), StepNotice.fromCode("earlier-steps-first"));
    assertEquals(
        "Please complete the previous checkout steps first.",
        StepNotice.EARLIER_STEPS_FIRST.sentence());
  }

  @Test
  @DisplayName("an unknown or absent code is no notice")
  void anUnknownOrAbsentCodeIsNoNotice() {
    assertEquals(Optional.empty(), StepNotice.fromCode("<b>Pay here</b>"));
    assertEquals(Optional.empty(), StepNotice.fromCode(null));
  }
}
