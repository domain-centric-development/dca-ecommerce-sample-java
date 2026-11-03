package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class RegistrationNoticeTest {

  private final RegistrationNoticeControllerAdvice advice =
      new RegistrationNoticeControllerAdvice();

  @Test
  @DisplayName("the notice a redirect brought along is shown as the success message")
  void theNoticeIsShownAsTheSuccessMessage() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(RegistrationNotice.KEY, "account-created");

    advice.showRegistrationNotice(model);

    assertEquals("Account created successfully! Welcome!", model.getAttribute("successMessage"));
  }

  @Test
  @DisplayName("an absent code shows nothing")
  void anAbsentCodeShowsNothing() {
    final ExtendedModelMap model = new ExtendedModelMap();

    advice.showRegistrationNotice(model);

    assertFalse(model.containsAttribute("successMessage"));
  }

  @Test
  @DisplayName("an unknown code shows nothing")
  void anUnknownCodeShowsNothing() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(RegistrationNotice.KEY, "<b>Pay here</b>");

    advice.showRegistrationNotice(model);

    assertFalse(model.containsAttribute("successMessage"));
  }

  @Test
  @DisplayName("the notice a redirect brought along is read with its sentence")
  void theNoticeARedirectBroughtIsReadWithItsSentence() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(RegistrationNotice.KEY, "account-created");

    assertEquals(Optional.of(RegistrationNotice.ACCOUNT_CREATED), RegistrationNotice.in(model));
    assertEquals(
        "Account created successfully! Welcome!", RegistrationNotice.ACCOUNT_CREATED.sentence());
  }

  @Test
  @DisplayName("an absent code is no notice")
  void anAbsentCodeIsNoNotice() {
    assertEquals(Optional.empty(), RegistrationNotice.in(new ExtendedModelMap()));
  }

  @Test
  @DisplayName("an unknown code is no notice")
  void anUnknownCodeIsNoNotice() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(RegistrationNotice.KEY, "<b>Pay here</b>");

    assertEquals(Optional.empty(), RegistrationNotice.in(model));
  }

  @Test
  @DisplayName("the redirect hands the notice to the page, the address stays as it is")
  void theRedirectHandsTheNoticeToThePage() {
    final RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

    assertEquals(
        "redirect:/products",
        RegistrationNotice.ACCOUNT_CREATED.redirectTo("/products", attributes));
    assertEquals(
        Map.of(RegistrationNotice.KEY, "account-created"), attributes.getFlashAttributes());
  }
}
