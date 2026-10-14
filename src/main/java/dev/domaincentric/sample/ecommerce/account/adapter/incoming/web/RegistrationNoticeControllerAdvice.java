package dev.domaincentric.sample.ecommerce.account.adapter.incoming.web;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Words the {@link RegistrationNotice} on whichever page a registration leads to.
 *
 * <p>The registration sends the visitor to the home page or to the page they came from — a page of
 * any context — so the notice is not read by one controller but here, for every page, and the
 * layout shows it. Spring has put the flash attributes addressed to the page on the model before
 * this runs; a page the notice is not addressed to gets none.
 */
@ControllerAdvice
public class RegistrationNoticeControllerAdvice {

  /** Puts the sentence of the registration notice a redirect handed the page on its model. */
  @ModelAttribute
  public void showRegistrationNotice(final Model model) {
    RegistrationNotice.show(model);
  }
}
