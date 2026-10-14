package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class RegistrationNoticeTest {

  @Test
  @DisplayName("the notice a redirect brought along is shown as the success message")
  void theNoticeIsShownAsTheSuccessMessage() {
    final ExtendedModelMap model = new ExtendedModelMap();
    model.addAttribute(RegistrationNotice.KEY, "account-created");

    RegistrationNotice.show(model);

    assertEquals("Account created successfully! Welcome!", model.getAttribute("successMessage"));
  }

  @Test
  @DisplayName("an unknown or absent code shows nothing")
  void anUnknownOrAbsentCodeShowsNothing() {
    final ExtendedModelMap model = new ExtendedModelMap();

    RegistrationNotice.show(model);
    model.addAttribute(RegistrationNotice.KEY, "<b>Pay here</b>");
    RegistrationNotice.show(model);

    assertFalse(model.containsAttribute("successMessage"));
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
